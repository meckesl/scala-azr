# Scala-AZR Project

This project is a simulation of the "Absolute Zero" self-play reasoning paradigm, adapted for lambda calculus. It uses a Scala application to manage the learning environment and a Python server to host a pre-trained Language Model (LLM) that acts as the learning agent.

## How it Works

1.  The **Python LLM Server** (`llm_server.py`) loads a pre-trained GPT-2 model and exposes it via a local API. This server is responsible for generating responses and updating the model's weights based on rewards.
2.  The **Scala Application** (`LambdaCalculusRL.scala`) orchestrates the self-play loop. It proposes new tasks, validates them, asks the Python server to solve them, calculates a reward, and sends that reward back to the server for training.

## Setting Up the Python LLM Server

Follow these steps to get the Python agent running.

### 1. Prerequisites

-   Python 3.8 or higher installed on your system.
-   `pip` for package installation.

### 2. Install Dependencies

Open your terminal or command prompt and run the following command to install the necessary Python libraries:

```bash
pip install torch transformers flask
```

### 3. Running the Server

Navigate to the root directory of this project in your terminal and run the server with the following command:

```bash
python llm_server.py
```

-   **First Run:** The first time you start the server, it will need to download the pre-trained GPT-2 model weights from Hugging Face. This is a one-time download of a few hundred megabytes.
-   **Successful Start:** Once running, you will see a message indicating that the server has started and is listening on `http://127.0.0.1:5000`.

**You must keep this terminal window open** for the Scala application to be able to communicate with the model.

### 4. Running the Scala Application

Once the Python server is running, you can start the main Scala application. The Scala code will then connect to the Python server to propose and solve tasks, driving the reinforcement learning loop.

```
