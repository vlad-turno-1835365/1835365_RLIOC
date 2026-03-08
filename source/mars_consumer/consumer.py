import os
import json
import asyncio
import threading
import time
import requests
import redis
import psycopg2
from confluent_kafka import Consumer, KafkaError
from fastapi import FastAPI, HTTPException
import uvicorn

DASHBOARD_API_URL = os.getenv('DASHBOARD_API_URL', 'http://localhost:8000/api/events')

# --- CONFIGURAZIONI ---
REDIS_HOST = os.getenv('REDIS_HOST', 'redis')
REDIS_PORT = int(os.getenv('REDIS_PORT', 6379))

DB_HOST = os.getenv('DB_HOST', 'postgres')
DB_PORT = os.getenv('DB_PORT', '5432')
DB_USER = os.getenv('DB_USER', 'marsuser')
DB_PASSWORD = os.getenv('DB_PASSWORD', 'marspassword')
DB_NAME = os.getenv('DB_NAME', 'marsdb')

# Inizializzazione Redis
try:
    redis_client = redis.Redis(host=REDIS_HOST, port=REDIS_PORT, decode_responses=True)
except Exception as e:
    print(f"[!] Errore connessione Redis: {e}")
    redis_client = None

# App FastAPI
app = FastAPI(title="Mars Consumer API", description="API per consultare gli ultimi dati telemetrici da Marte")

def get_db_connection():
    try:
        conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            user=DB_USER,
            password=DB_PASSWORD,
            dbname=DB_NAME
        )
        return conn
    except Exception as e:
        print(f"[!] Errore connessione DB: {e}")
        return None

def init_db():
    conn = None
    for i in range(15):
        conn = get_db_connection()
        if conn:
            break
        print(f"[*] Attesa DB, ritento tra 2 secondi... ({i+1}/15)")
        time.sleep(2)
        
    if not conn:
        print("[!] Impossibile connettersi al DB per l'inizializzazione.")
        return
    
    try:
        cur = conn.cursor()
        cur.execute("""
            CREATE TABLE IF NOT EXISTS rules (
                id SERIAL PRIMARY KEY,
                sensor_name VARCHAR(50) UNIQUE NOT NULL,
                min_threshold FLOAT,
                max_threshold FLOAT
            )
        """)
        
        cur.execute("SELECT COUNT(*) FROM rules")
        if cur.fetchone()[0] == 0:
            # Seed base rules
            cur.execute("INSERT INTO rules (sensor_name, min_threshold, max_threshold) VALUES ('temperature', -100, 50)")
            cur.execute("INSERT INTO rules (sensor_name, min_threshold, max_threshold) VALUES ('pressure', 0, 2000)")
            print("[*] Regole base inserite nel database.")
            
        conn.commit()
        cur.close()
    except Exception as e:
        print(f"[!] Errore setup DB: {e}")
    finally:
        conn.close()

def get_rule_for_sensor(sensor_name):
    conn = get_db_connection()
    if not conn:
        return None
    
    try:
        cur = conn.cursor()
        cur.execute("SELECT min_threshold, max_threshold FROM rules WHERE sensor_name = %s", (sensor_name,))
        row = cur.fetchone()
        cur.close()
        
        if row:
            return {'min_threshold': row[0], 'max_threshold': row[1]}
    except Exception as e:
        print(f"[!] Errore lettura regole: {e}")
    finally:
        conn.close()
    return None

def apply_rule(sensor_name, value):
    rule = get_rule_for_sensor(sensor_name)
    if not rule:
        return "OK" # Nessuna regola, quindi OK
    
    min_t = rule.get('min_threshold')
    max_t = rule.get('max_threshold')
    
    try:
        num_val = float(value)
        if min_t is not None and num_val < min_t:
            return "WARNING_LOW"
        if max_t is not None and num_val > max_t:
            return "WARNING_HIGH"
    except (ValueError, TypeError):
        pass
    
    return "OK"

def process_event(payload):
    """
    Estrazione, validazione, applicazione regole e cache.
    """
    try:
        event_id = payload['event_id']
        timestamp = payload['timestamp']
        sensor_name = payload['sensor_name']
        value = payload['value']
        
        unit = payload.get('unit', 'N/A')
        
        # 1. Applica Regole
        status = apply_rule(sensor_name, value)
        payload['status'] = status
        
        print(f"\n[+] Nuovo Evento: {sensor_name} = {value} {unit} [{timestamp}] - Regola applicata: {status}")

        # 2. Salva in Cache
        if redis_client:
            try:
                redis_client.set(f"sensor_latest:{sensor_name}", json.dumps(payload))
            except Exception as e:
                print(f"[-] Errore salvataggio su Redis: {e}")

        # 3. Inoltro API (se necessario)
        try:
            response = requests.post(DASHBOARD_API_URL, json=payload, timeout=2.0)
            if response.status_code == 200:
                print("    -> Inviato con successo alla Dashboard API.")
            else:
                print(f"    -> [!] Errore API: ricevo status {response.status_code}")
        except requests.exceptions.RequestException as e:
            print(f"    -> [!] Impossibile contattare la Dashboard API ({DASHBOARD_API_URL}).")

    except KeyError as e:
        print(f"[-] Errore di validazione: manca il campo {e} nel payload.")

def run_kafka_consumer():
    kafka_broker = os.getenv('KAFKA_BROKER', 'kafka:9092')
    kafka_topic = os.getenv('KAFKA_TOPIC', 'mars-telemetry-events')
    group_id = os.getenv('KAFKA_GROUP_ID', 'mars_consumer_group')

    conf = {
        'bootstrap.servers': kafka_broker,
        'group.id': group_id,
        'auto.offset.reset': 'earliest'
    }

    try:
        consumer = Consumer(conf)
        consumer.subscribe([kafka_topic])
        print(f"[*] Consumer (thread) avviato. In attesa di eventi su '{kafka_topic}'...")

        while True:
            msg = consumer.poll(timeout=1.0)
            if msg is None:
                continue
            if msg.error():
                if msg.error().code() == KafkaError._PARTITION_EOF:
                    continue
                else:
                    print(f"[!] Errore Kafka: {msg.error()}")
                    break

            try:
                raw_value = msg.value().decode('utf-8')
                payload = json.loads(raw_value)
                process_event(payload)
            except json.JSONDecodeError:
                print(f"[-] Errore JSON Decode. Contenuto: {msg.value()}")
                
    except Exception as e:
        print(f"[*] Chiusura Kafka Consumer: {e}")
    finally:
        if 'consumer' in locals():
            consumer.close()

@app.on_event("startup")
def startup_event():
    # Inizializza il DB all'avvio
    init_db()
    
    # Avvia il Kafka Consumer su un Thread separato per non bloccare FastAPI
    kafka_thread = threading.Thread(target=run_kafka_consumer, daemon=True)
    kafka_thread.start()

# --- Endpoint API per DashboardApiService ---

@app.get("/api/latest/{sensor_name}")
def get_latest_sensor_data(sensor_name: str):
    if not redis_client:
        raise HTTPException(status_code=500, detail="Redis cache not available")
        
    data = redis_client.get(f"sensor_latest:{sensor_name}")
    if data:
        return json.loads(data)
    else:
        raise HTTPException(status_code=404, detail="No data found for this sensor")

@app.get("/api/rules")
def get_all_rules():
    conn = get_db_connection()
    if not conn:
        raise HTTPException(status_code=500, detail="Database not available")
        
    try:
        cur = conn.cursor()
        cur.execute("SELECT sensor_name, min_threshold, max_threshold FROM rules")
        rows = cur.fetchall()
        rules = [{"sensor_name": r[0], "min_threshold": r[1], "max_threshold": r[2]} for r in rows]
        cur.close()
        return rules
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        conn.close()

if __name__ == '__main__':
    # Puoi avviare il file direttamente o tramite uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)