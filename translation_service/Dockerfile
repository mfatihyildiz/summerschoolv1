FROM python:3.10-slim

WORKDIR /app
COPY translation_service.py .

RUN pip install --no-cache-dir Flask deep-translator langdetect gunicorn

EXPOSE 5002
CMD ["gunicorn","-b","0.0.0.0:5002","translation_service:app"]