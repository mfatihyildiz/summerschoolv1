from flask import Flask, request, jsonify
from deep_translator import GoogleTranslator
from langdetect import detect
from functools import lru_cache
import os

app = Flask(__name__)

DEFAULT_TARGET = os.getenv("DEFAULT_TARGET", "en")

@lru_cache(maxsize=50000)  # cache across process lifetime
def translate_cached(text: str, target: str) -> str:
    if not text or not text.strip():
        return text
    try:
        lang = detect(text)
        if lang.lower() == target.lower():
            return text
        return GoogleTranslator(source="auto", target=target).translate(text)
    except Exception:
        return text

@app.get("/health")
def health():
    return jsonify({"status":"ok"}), 200

@app.post("/translate")
def translate_single():
    data = request.get_json(force=True)
    text = data.get("text","")
    target = data.get("target", DEFAULT_TARGET)
    return jsonify({"text_en": translate_cached(text, target)})

@app.post("/translate/batch")
def translate_batch():
    data = request.get_json(force=True)
    texts = data.get("texts", [])
    target = data.get("target", DEFAULT_TARGET)
    # de-duplicate within request, then map back
    uniq = list(dict.fromkeys(texts))
    trans_map = {t: translate_cached(t, target) for t in uniq}
    return jsonify({"texts_en": [trans_map.get(t, t) for t in texts]})

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5002)
