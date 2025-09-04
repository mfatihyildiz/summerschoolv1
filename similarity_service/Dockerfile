FROM pytorch/pytorch:2.7.1-cuda11.8-cudnn9-runtime

WORKDIR /app
COPY similarity_service.py .

RUN pip install --no-cache-dir Flask sentence-transformers langdetect deep-translator requests gunicorn \
 && python -c "from sentence_transformers import SentenceTransformer; SentenceTransformer('intfloat/e5-large')"
# && python -c "from sentence_transformers import SentenceTransformer; SentenceTransformer('BAAI/bge-large-en-v1.5')"

EXPOSE 5001
CMD ["gunicorn","-b","0.0.0.0:5001","similarity_service:app"]