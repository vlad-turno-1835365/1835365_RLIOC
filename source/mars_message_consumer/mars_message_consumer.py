import os
import json
import time
import requests
from confluent_kafka import Consumer, KafkaError

KAFKA_BROKER = os.getenv('KAFKA_BROKER', 'kafka:9092')
KAFKA_TOPIC = os.getenv('KAFKA_TOPIC', 'mars-telemetry-events')
KAFKA_GROUP_ID = os.getenv('KAFKA_GROUP_ID', 'mars_consumer_group')

DB_MANAGER_URL = os.getenv('DB_MANAGER_URL', 'http://mars_db_manager:8000')
MEMORY_CACHE_URL = os.getenv('MEMORY_CACHE_URL', 'http://mars_memory_cache:8000')
API_GATEWAY_URL = os.getenv('API_GATEWAY_URL', 'http://mars_api_gateway:8000')

def process_event(payload):
    try:
        sensor_name = payload['sensor_name']
        value = payload['value']
        timestamp = payload.get('timestamp')
        unit = payload.get('unit', 'N/A')
        
        triggered_actions = []
        try:
            resp = requests.post(
                f"{DB_MANAGER_URL}/api/evaluate", 
                json={"sensor_name": sensor_name, "value": float(value)},
                timeout=2.0
            )
            if resp.status_code == 200:
                triggered_actions = resp.json().get("triggered_actions", [])
        except Exception as e:
            print(f"[!] Errore contattando DB Manager: {e}")
            
        payload['triggered_actions'] = triggered_actions
        
        print(f"\n[+] Nuovo Evento: {sensor_name} = {value} {unit} [{timestamp}]")
        if triggered_actions:
            print(f"    -> Azioni: {triggered_actions}")
            
            # Execute triggered actions via API Gateway
            print(f"    -> 🔄 Inizio esecuzione azioni...")
            for action in triggered_actions:
                try:
                    actuator_name = action['actuator']
                    actuator_state = action['action']
                    print(f"    -> 📤 Tentativo: {actuator_name} = {actuator_state} verso {API_GATEWAY_URL}")
                    
                    response = requests.post(
                        f"{API_GATEWAY_URL}/api/actuators/{actuator_name}",
                        json={"state": actuator_state},
                        timeout=2.0
                    )
                    
                    print(f"    -> 📡 Response status: {response.status_code}")
                    if response.status_code == 200:
                        print(f"    -> ✅ Eseguito: {actuator_name} = {actuator_state}")
                        print(f"    -> 📄 Response: {response.text}")
                    else:
                        print(f"    -> ❌ Errore eseguendo {actuator_name}: {response.status_code}")
                        print(f"    -> 📄 Error response: {response.text}")
                        
                except Exception as e:
                    print(f"    -> ❌ Errore esecuzione azione {action}: {e}")
                    print(f"    -> 🔍 Exception type: {type(e).__name__}")
            
            print(f"    -> ✅ Fine esecuzione azioni")

        try:
            requests.post(
                f"{MEMORY_CACHE_URL}/api/state",
                json={"sensor_name": sensor_name, "data": payload},
                timeout=2.0
            )
        except Exception as e:
            print(f"[-] Errore salvataggio su Memory Cache: {e}")

        try:
            response = requests.post(f"{API_GATEWAY_URL}/api/events", json=payload, timeout=2.0)
            if response.status_code != 200:
                print(f"    -> [!] Errore API Gateway: ricevo status {response.status_code}")
        except requests.exceptions.RequestException:
            print(f"    -> [!] Impossibile contattare API Gateway.")

    except KeyError as e:
        print(f"[-] Errore di validazione: manca il campo {e} nel payload.")

def run():
    conf = {
        'bootstrap.servers': KAFKA_BROKER,
        'group.id': KAFKA_GROUP_ID,
        'auto.offset.reset': 'earliest'
    }

    consumer = Consumer(conf)
    consumer.subscribe([KAFKA_TOPIC])
    print(f"[*] Consumer avviato. In attesa di eventi su '{KAFKA_TOPIC}'...")

    try:
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
                
    except KeyboardInterrupt:
        print("[*] Chiusura richiesta dall'utente...")
    except Exception as e:
        print(f"[*] Errore imprevisto: {e}")
    finally:
        consumer.close()

if __name__ == "__main__":
    time.sleep(5)
    run()
