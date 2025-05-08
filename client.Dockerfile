# client.Dockerfile
FROM python:3.10-slim

WORKDIR /usr/src/app

COPY src/tcp_client.py .

RUN pip install --no-cache-dir -r requirements.txt || true  # אם אין requirements, זה לא יכשיל

CMD ["python3", "tcp_client.py"]
