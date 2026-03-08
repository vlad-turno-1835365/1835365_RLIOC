import os
import json
import requests
from confluent_kafka import Consumer, KafkaError

# URL dell'API a cui inviare i dati. 
# Tramite Docker Compose, i tuoi compagni potranno impostare questo valore al nome del container dell'API.
DASHBOARD_API_URL = os.getenv('DASHBOARD_API_URL', 'http://localhost:8000/api/events')

def process_event(payload):
    """
    Funzione dedicata all'estrazione, validazione e inoltro dell'evento standardizzato.
    """
    try:
        # Estrazione dei campi obbligatori
        event_id = payload['event_id']
        timestamp = payload['timestamp']
        sensor_name = payload['sensor_name']
        value = payload['value']
        
        # Estrazione dei campi opzionali
        unit = payload.get('unit', 'N/A')
        status = payload.get('status', 'unknown')

        print(f"\n[+] Nuovo Evento: {sensor_name} = {value} {unit} [{timestamp}]")

        # --- NOVITÀ: Inoltro dei dati al Microservizio 2 ---
        try:
            # Facciamo una POST all'endpoint esposto dal tuo servizio FastAPI
            response = requests.post(DASHBOARD_API_URL, json=payload, timeout=2.0)
            
            if response.status_code == 200:
                print("    -> Inviato con successo alla Dashboard API.")
            else:
                print(f"    -> [!] Errore API: ricevo status {response.status_code}")
                
        except requests.exceptions.RequestException as e:
            print(f"    -> [!] Impossibile contattare la Dashboard API ({DASHBOARD_API_URL}). È accesa?")

    except KeyError as e:
        print(f"[-] Errore di validazione: manca il campo {e} nel payload.")

def main():
    kafka_broker = os.getenv('KAFKA_BROKER', 'kafka:9092')
    kafka_topic = os.getenv('KAFKA_TOPIC', 'mars-telemetry-events')
    group_id = os.getenv('KAFKA_GROUP_ID', 'mars_consumer_group')

    conf = {
        'bootstrap.servers': kafka_broker,
        'group.id': group_id,
        'auto.offset.reset': 'earliest'
    }

    consumer = Consumer(conf)
    consumer.subscribe([kafka_topic])

    print(f"[*] Consumer avviato. In attesa di eventi su '{kafka_topic}'...")

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
                
                # Chiama la funzione che processa e inoltra l'evento
                process_event(payload)
                
            except json.JSONDecodeError:
                print(f"[-] Errore JSON Decode. Contenuto: {msg.value()}")
                
    except KeyboardInterrupt:
        print("\n[*] Interruzione da tastiera. Chiusura in corso...")
    finally:
        consumer.close()

if __name__ == '__main__':
    main()