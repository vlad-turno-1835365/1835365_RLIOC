FROM python:3.9-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY mars_memory_cache.py .

CMD ["uvicorn", "mars_memory_cache:app", "--host", "0.0.0.0", "--port", "8000"]
