import json

def parse_llm_json(text):
    try:
        return json.loads(text)
    except:
        return None
    