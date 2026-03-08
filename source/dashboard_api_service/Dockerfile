# Usa un'immagine Python ufficiale e leggera
FROM python:3.11-slim

# Imposta la cartella di lavoro nel container
WORKDIR /app

# Copia i requisiti e installali
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copia il codice dell'API
COPY main.py .

# Esponi la porta 8000 (quella usata da FastAPI)
EXPOSE 8000

# Avvia l'applicazione con Uvicorn
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]