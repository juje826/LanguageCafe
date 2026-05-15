import os
from openai import OpenAI
from dotenv import load_dotenv

# Load .env for API key
load_dotenv()

# Create openAI client when server starts
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))


SYSTEM_PROMPT = """
You are an AI language tutor inside a roleplay conversation.

GENERAL RULES:
- stay in role
- adapt to unexpected answers
- keep responses concise

LANGUAGE RULES:
- use CEFR level A1
- use short sentences
- avoid complex grammar
"""

def generate_chat_response(prompt):
    """
    Send message to the LLM and return the response
    """

    response = client.chat.completions.create(
        model="gpt-4o", # or the other model
        messages=[{"role": "system", "content": SYSTEM_PROMPT},
                  {"role": "user", "content": prompt}],
        temperature=0.3
    )

    return response.choices[0].message.content