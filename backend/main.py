from fastapi import FastAPI
from pydantic import BaseModel
from llm_service import generate_chat_response
from memory.session_store import get_session
from scenarios.scenario_engine import create_prompt, update_goals
from utils.json_parser import parse_llm_json


app = FastAPI()

class ChatRequest(BaseModel):
    message: str
    session_id: str
    scenario_id: str
    native_language: str
    target_language: str
class TranslationRequest(BaseModel):
    text: str
    target_language: str
    source_language: str = "auto" # Optional: Defaults to auto-detect if not provided
@app.get("/")
def root():
    return {"status": "LanguageCafe backend running"}

@app.post("/chat")
def chat(request: ChatRequest):
    state = get_session(request.session_id)

    if state["scenario"] is None:
        state["scenario"] = request.scenario_id
        
    if state.get("native_language") is None:
        state["native_language"] = request.native_language

    if state.get("target_language") is None:
        state["target_language"] = request.target_language

    # Create prompt from user message
    user_message = request.message
    prompt = create_prompt(state, user_message)

    # parse JSON and try again if invalid JSON is returned
    MAX_RETRIES = 2
    llm_output = None
    llm_raw = None

    for _ in range(MAX_RETRIES):
        llm_raw = generate_chat_response(prompt)

        print("RAW LLM OUTPUT:")
        print(llm_raw)

        llm_output = parse_llm_json(llm_raw)
        if llm_output:
            break
    
    if llm_output is None:
        print("FAILED TO PARSE JSON AFTER RETRIES")
        return {
            "response": "Sorry, something went wrong. Please try again.",
            "detected_goal": None,
            "debug": llm_raw
        }

    # store conversation state
    state["chat_history"].append({
        "role": "student",
        "content": request.message
    })
@app.post("/translate")
def translate_text(request: TranslationRequest):
    if request.source_language == "auto":
        prompt = f"Translate the following text into {request.target_language}. Only return the translated text, nothing else.\n\nText: '{request.text}'"
    else:
        prompt = f"Translate the following text from {request.source_language} to {request.target_language}. Only return the translated text, nothing else.\n\nText: '{request.text}'"

    try:
        translation_result = generate_chat_response(prompt)
        clean_translation = translation_result.strip().strip("'\"")
        
        return {
            "status": "success",
            "original_text": request.text,
            "target_language": request.target_language,
            "translation": clean_translation
        }
    except Exception as e:
        print(f"TRANSLATION ERROR: {e}")
        return {
            "status": "error",
            "message": "Failed to translate text. Please try again."
        }
    # update goals
    update_goals(state, llm_output)

    return llm_output

