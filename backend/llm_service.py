import os
from openai import OpenAI
from dotenv import load_dotenv

# Load .env for API key
load_dotenv()

# Create openAI client when server starts
client = OpenAI(api_key=os.getenv("LLM_API_KEY"),
                base_url="https://litellm.nolai.nl/v1")


SYSTEM_PROMPT = """
You are a conversational partner in a language learning app called Language Cafe.
Help users practice languages, answer questions, and keep responses concise. Adapt
responses to the learning level of the users.
"""

def generate_chat_response(user_message):
    """
    Send message to the LLM and return the response
    """

    response = client.chat.completions.create(
        model="gemma3:27b", # or the other model
        messages=[{"role": "system", "content": SYSTEM_PROMPT},
                  {"role": "user", "content": user_message}],
        temperature=0.7
    )

    return response.choices[0].message.content