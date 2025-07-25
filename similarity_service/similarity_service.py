from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer, util
import torch

app = Flask(__name__)
model = SentenceTransformer('paraphrase-multilingual-MiniLM-L12-v2')

@app.route('/similarity', methods=['POST'])
def similarity():
    data = request.json
    weeks1 = data['desc1'].split(' | ')
    weeks2 = data['desc2'].split(' | ')
    # Pad to 14 weeks if needed
    while len(weeks1) < 14: weeks1.append("")
    while len(weeks2) < 14: weeks2.append("")

    # Encode all weeks at once
    emb1 = model.encode(weeks1, convert_to_tensor=True)
    emb2 = model.encode(weeks2, convert_to_tensor=True)

    # Compute cosine similarity matrix (14x14)
    sim_matrix = util.pytorch_cos_sim(emb1, emb2)  # shape: (14, 14)

    # For each week in home, find the best match in external
    max_sim_home = torch.max(sim_matrix, dim=1).values  # shape: (14,)
    # For each week in external, find the best match in home
    max_sim_external = torch.max(sim_matrix, dim=0).values  # shape: (14,)

    # Average for final score (symmetric)
    avg_sim = (max_sim_home.mean().item() + max_sim_external.mean().item()) / 2

    # Optionally, you can also return the per-week best matches
    return jsonify({
        'max_similarities_home': max_sim_home.tolist(),
        'max_similarities_external': max_sim_external.tolist(),
        'average_similarity': avg_sim
    })

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5001)
