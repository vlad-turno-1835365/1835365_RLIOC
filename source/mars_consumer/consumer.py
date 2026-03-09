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
        print("[!] Impossibile connettersi al DB.")
        return
    
    try:
        cur = conn.cursor()
        # Schema database: IF <sensor_name> <operator> <value> THEN set <actuator_name> to <actuator_action>
        cur.execute("""
            CREATE TABLE IF NOT EXISTS automation_rules (
                id SERIAL PRIMARY KEY,
                sensor_name VARCHAR(100) NOT NULL,
                operator VARCHAR(2) NOT NULL,
                threshold_value FLOAT NOT NULL,
                actuator_name VARCHAR(100) NOT NULL,
                actuator_action VARCHAR(3) NOT NULL
            )
        """)
        
        cur.execute("SELECT COUNT(*) FROM automation_rules")
        if cur.fetchone()[0] == 0:
            # Esempio insert di una regola un po' a caso
            cur.execute("INSERT INTO automation_rules (sensor_name, operator, threshold_value, actuator_name, actuator_action) VALUES ('temperature', '>', 35.0, 'cooling_fan', 'ON')")
            print("[*] Regole base inserite nel database.")
            
        conn.commit()
        cur.close()
    except Exception as e:
        print(f"[!] Errore setup DB: {e}")
    finally:
        conn.close()

def evaluate_rules(sensor_name, value):
    conn = get_db_connection()
    if not conn:
        return []
    
    triggered_actions = []
    try:
        cur = conn.cursor()
        cur.execute("SELECT operator, threshold_value, actuator_name, actuator_action FROM automation_rules WHERE sensor_name = %s", (sensor_name,))
        rules = cur.fetchall()
        cur.close()
        
        num_val = float(value)
        
        # Valuta le regole presenti per <sensor_name>
        for rule in rules:
            operator, threshold, actuator_name, actuator_action = rule
            condition_met = False
            
            if operator == '<' and num_val < threshold: condition_met = True
            elif operator == '<=' and num_val <= threshold: condition_met = True
            elif operator == '=' and num_val == threshold: condition_met = True
            elif operator == '>' and num_val > threshold: condition_met = True
            elif operator == '>=' and num_val >= threshold: condition_met = True
            
            if condition_met:
                triggered_actions.append({"actuator": actuator_name, "action": actuator_action})
                print(f"[!] AUTOMAZIONE: {sensor_name} {operator} {threshold} -> Imposto {actuator_name} a {actuator_action}")
                
    except Exception as e:
        print(f"[!] Errore durante la lettura/attuazione delle regole: {e}")
    finally:
        conn.close()
        
    return triggered_actions

def process_event(payload):
    try:
        event_id = payload.get('event_id')
        timestamp = payload.get('timestamp')
        sensor_name = payload['sensor_name']
        value = payload['value']
        unit = payload.get('unit', 'N/A')
        
        actions = evaluate_rules(sensor_name, value) # Deriva le eventuali azioni da eseguire
        payload['triggered_actions'] = actions # Aggiungile al payload per la dashboard
        
        print(f"\n[+] Nuovo Evento: {sensor_name} = {value} {unit} [{timestamp}]")

        if redis_client:
            try:
                redis_client.set(f"sensor_latest:{sensor_name}", json.dumps(payload)) # Salva in cache
            except Exception as e:
                print(f"[-] Errore salvataggio su Redis: {e}")

        try:
            response = requests.post(DASHBOARD_API_URL, json=payload, timeout=2.0) # Inoltra il payload alla dashboard
            if response.status_code != 200:
                print(f"    -> [!] Errore API: ricevo status {response.status_code}")
        except requests.exceptions.RequestException:
            print(f"    -> [!] Impossibile contattare la Dashboard API.")

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
        cur.execute("SELECT id, sensor_name, operator, threshold_value, actuator_name, actuator_action FROM automation_rules")
        rows = cur.fetchall()
        rules = [
            {
                "id": r[0], 
                "sensor_name": r[1], 
                "operator": r[2], 
                "threshold": r[3],
                "actuator": r[4],
                "action": r[5]
            } for r in rows
        ]
        cur.close()
        return rules
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        conn.close()