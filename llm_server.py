from flask import Flask, request, jsonify
from transformers import AutoConfig, AutoTokenizer, AutoModelForCausalLM

# --- Model Configuration ---
# We choose a standard, small architecture (like GPT-2) as our base.
MODEL_ARCH = "gpt2"

print("1. Configuring a blank-slate model...")
# Load the standard configuration for the chosen architecture.
config = AutoConfig.from_pretrained(MODEL_ARCH)

# IMPORTANT: Instead of loading pre-trained weights, we initialize a new model
# FROM THE CONFIGURATION. This creates the model with random weights.
# This is the "blank-slate" LLM.
model = AutoModelForCausalLM.from_config(config)

# We still need a tokenizer to convert text to numbers the model understands.
# We can use the standard one for the architecture.
tokenizer = AutoTokenizer.from_pretrained(MODEL_ARCH)
# Add a padding token to handle batches of different lengths.
tokenizer.pad_token = tokenizer.eos_token

print("2. Model is initialized with random weights.")

# --- API Server ---
app = Flask(__name__)

@app.route("/generate", methods=["POST"])
def generate():
    try:
        data = request.get_json()
        prompt = data["prompt"]

        # Tokenize the input prompt
        inputs = tokenizer(prompt, return_tensors="pt")

        # Generate text using the model.
        # The output will be nonsensical because the weights are random.
        outputs = model.generate(
            inputs.input_ids,
            max_new_tokens=20,  # Keep the output short for this example
            pad_token_id=tokenizer.pad_token_id
        )

        # Decode the generated token IDs back to text
        response_text = tokenizer.decode(outputs[0], skip_special_tokens=True)

        # We only want the newly generated part, not the original prompt
        generated_text = response_text[len(prompt):]

        return jsonify({"generated_text": generated_text})

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    print("3. Starting Flask server at http://127.0.0.1:5000")
    # Running on port 5000
    app.run(host="0.0.0.0", port=5000)