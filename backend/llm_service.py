import os
from openai import OpenAI
from dotenv import load_dotenv

# Load .env for API key
load_dotenv()

# Create openAI client when server starts
client = OpenAI(api_key=os.getenv("LLM_API_KEY"),
                base_url="https://litellm.nolai.nl/v1")


SYSTEM_PROMPT = """
You are an AI language tutor inside a roleplay conversation.

GENERAL RULES:
- stay in role
- adapt to unexpected answers
- guide conversation toward the scenario goal
- prioritize communication over perfection
- keep responses concise

LANGUAGE RULES:
- talk back in the language that is talked to you
- use CEFR level A1
- use short sentences
- avoid complex grammar
"""

def generate_chat_response(prompt):
    """
    Send message to the LLM and return the response
    """

    response = client.chat.completions.create(
        model="gemma3:27b", # or the other model
        messages=[{"role": "system", "content": SYSTEM_PROMPT},
                  {"role": "user", "content": prompt}],
        temperature=0.3
    )

    return response.choices[0].message.content