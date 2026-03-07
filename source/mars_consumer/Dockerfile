# Usa un'immagine Python ufficiale e leggera
FROM python:3.11-slim

# Imposta la directory di lavoro dentro il container
WORKDIR /app

# Copia il file dei requisiti e installa le dipendenze
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copia il codice sorgente nel container
COPY consumer.py .

# Imposta le variabili d'ambiente di default (verranno sovrascritte dal docker-compose dei tuoi compagni)
ENV KAFKA_BROKER="kafka:9092"
ENV KAFKA_TOPIC="mars_telemetry"

# Comando per avviare il microservizio
CMD ["python", "consumer.py"]