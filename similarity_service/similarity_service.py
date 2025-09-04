from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer, util
import requests, os, torch

app = Flask(__name__)

EMBEDDING_MODEL = os.getenv("EMBEDDING_MODEL", "intfloat/e5-large")
TRANSLATION_BASE_URL = os.getenv("TRANSLATION_BASE_URL", "http://translation:5002")
BATCH_ENDPOINT = f"{TRANSLATION_BASE_URL}/translate/batch"

model = SentenceTransformer(EMBEDDING_MODEL)
session = requests.Session()

def ensure_14_weeks(desc: str) -> list[str]:
    desc = (desc or "").strip()
    if not desc:
        return [""] * 14
    sep = ";" if ";" in desc else " | "
    parts = [p.strip() for p in desc.split(sep)]
    parts = [p for p in parts if p is not None]
    if len(parts) < 14:
        parts += [""] * (14 - len(parts))
    return parts[:14]

def translate_to_en_both(weeks1: list[str], weeks2: list[str]) -> tuple[list[str], list[str]]:
    idx1 = [i for i, w in enumerate(weeks1) if w.strip()]
    idx2 = [i for i, w in enumerate(weeks2) if w.strip()]
    texts = [weeks1[i] for i in idx1] + [weeks2[i] for i in idx2]
    if not texts:
        return weeks1, weeks2
    try:
        r = session.post(BATCH_ENDPOINT, json={"texts": texts, "target": "en"}, timeout=60)
        r.raise_for_status()
        tr = r.json().get("texts_en", texts)
        t1, t2 = tr[:len(idx1)], tr[len(idx1):]
        out1, out2 = weeks1[:], weeks2[:]
        for j, i in enumerate(idx1): out1[i] = t1[j]
        for j, i in enumerate(idx2): out2[i] = t2[j]
        return out1, out2
    except Exception:
        return weeks1, weeks2

@app.get("/health")
def health():
    return jsonify({"status": "ok"}), 200

@app.get("/similarity")
def similarity_get():
    return jsonify({"message": "POST only. Send JSON: {desc1, desc2}"}), 200

@app.post("/similarity")
def similarity():
    data = request.get_json(force=True)
    weeks1 = ensure_14_weeks(data.get("desc1", ""))
    weeks2 = ensure_14_weeks(data.get("desc2", ""))

    weeks1_en, weeks2_en = translate_to_en_both(weeks1, weeks2)

    emb1 = model.encode(weeks1_en, convert_to_tensor=True, normalize_embeddings=True)
    emb2 = model.encode(weeks2_en, convert_to_tensor=True, normalize_embeddings=True)

    sim_matrix = util.pytorch_cos_sim(emb1, emb2)
    max_sim_home = torch.max(sim_matrix, dim=1).values
    max_sim_external = torch.max(sim_matrix, dim=0).values
    avg_sim = (max_sim_home.mean().item() + max_sim_external.mean().item()) / 2

    return jsonify({
        "max_similarities_home": max_sim_home.tolist(),
        "max_similarities_external": max_sim_external.tolist(),
        "average_similarity": avg_sim
    })

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001)
