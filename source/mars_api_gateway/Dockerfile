FROM python:3.9-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY mars_api_gateway.py .

CMD ["uvicorn", "mars_api_gateway:app", "--host", "0.0.0.0", "--port", "8000"]
