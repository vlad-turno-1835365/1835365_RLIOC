FROM python:3.9-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY mars_db_manager.py .

CMD ["uvicorn", "mars_db_manager:app", "--host", "0.0.0.0", "--port", "8000"]
