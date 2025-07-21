FROM pytorch/pytorch:2.7.1-cuda11.8-cudnn9-runtime

WORKDIR /app

COPY similarity_service.py .

RUN pip install flask sentence-transformers

EXPOSE 5001

CMD ["python", "similarity_service.py"]