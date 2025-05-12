# client.Dockerfile
FROM python:3.10-slim

WORKDIR /usr/src/app

COPY src/tcp_client.py .

RUN pip install --no-cache-dir -r requirements.txt || true

ENTRYPOINT ["python3", "tcp_client.py"]
CMD ["127.0.0.1", "12345"]