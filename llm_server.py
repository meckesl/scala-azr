from flask import Flask, request, jsonify
from transformers import AutoConfig, AutoTokenizer, AutoModelForCausalLM
import torch

# --- Model Configuration ---
# We are now loading a more capable, instruction-tuned model specialized for code.
MODEL_ARCH = "Qwen/Qwen2-0.5B-Instruct"

print(f"1. Loading pre-trained model and tokenizer for {MODEL_ARCH}...")
# Load the standard configuration and tokenizer for the chosen architecture.
config = AutoConfig.from_pretrained(MODEL_ARCH)
tokenizer = AutoTokenizer.from_pretrained(MODEL_ARCH)
tokenizer.pad_token = tokenizer.eos_token

# Load the pre-trained weights for the new model.
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
print(f"Using device: {device}")
model = AutoModelForCausalLM.from_pretrained(
    MODEL_ARCH,
    torch_dtype=torch.bfloat16
).to(device)

print("2. Model is initialized with pre-trained weights.")

# --- Training Setup ---
optimizer = torch.optim.AdamW(model.parameters(), lr=5e-5)
training_buffer = {}
print("3. Optimizer initialized.")

# --- API Server ---
app = Flask(__name__)

@app.route("/generate", methods=["POST"])
def generate():
    global training_buffer
    try:
        data = request.get_json()
        prompt = data["prompt"]
        input_ids = tokenizer(prompt, return_tensors="pt").input_ids.to(device)

        log_probs = []
        generated_ids = []
        
        # Manual generation loop to ensure the computation graph is maintained
        current_ids = input_ids
        for _ in range(20):  # max_new_tokens
            # Perform a forward pass to get the logits
            outputs = model(current_ids)
            
            # Get the logits for the very last token in the sequence
            next_token_logits = outputs.logits[:, -1, :]
            
            # Create a probability distribution from the logits
            dist = torch.distributions.Categorical(logits=next_token_logits)
            
            # Sample the next token from the distribution
            next_token = dist.sample()
            
            # Get the log probability of the sampled token and store it
            log_probs.append(dist.log_prob(next_token))
            
            # Add the new token to our list of generated tokens
            generated_ids.append(next_token.item())
            
            # Concatenate the new token to the input for the next iteration
            current_ids = torch.cat([current_ids, next_token.unsqueeze(-1)], dim=-1)

        # Sum the log probabilities for the entire generated sequence.
        # This tensor is now correctly attached to the computation graph.
        sequence_log_prob = torch.stack(log_probs).sum()

        # Cache the log probability for the /train endpoint
        training_buffer['sequence_log_prob'] = sequence_log_prob

        # Decode the generated token IDs into text
        generated_text = tokenizer.decode(generated_ids, skip_special_tokens=True)

        return jsonify({"generated_text": generated_text})

    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/train", methods=["POST"])
def train():
    global training_buffer
    try:
        data = request.get_json()
        reward = data["reward"]

        if 'sequence_log_prob' not in training_buffer:
            return jsonify({"error": "No generation data to train on. Call /generate first."}), 400

        log_prob = training_buffer.pop('sequence_log_prob')

        # REINFORCE algorithm: loss is the negative of the log probability multiplied by the reward
        loss = -log_prob * reward

        # Perform backpropagation and update the model's weights
        optimizer.zero_grad()
        loss.backward()
        optimizer.step()

        return jsonify({"status": "training_step_complete", "loss": loss.item()})

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    print("4. Starting Flask server at http://127.0.0.1:5000")
    app.run(host="0.0.0.0", port=5000)
