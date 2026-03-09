FROM python:3.9-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY mars_message_consumer.py .

CMD ["python", "-u", "mars_message_consumer.py"]
