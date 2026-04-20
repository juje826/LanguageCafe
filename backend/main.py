from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from llm_service import generate_chat_response
from memory.session_store import get_session
from scenarios.scenario_engine import create_prompt, update_goals
from utils.json_parser import parse_llm_json

app = FastAPI()

# --- 1. Pydantic Models ---
class ChatRequest(BaseModel):
    message: str
    session_id: str
    scenario_id: str
    native_language: str
    target_language: str

class TranslationRequest(BaseModel):
    text: str
    target_language: str
    source_language: str = "auto"

# --- 2. Scenario Data (Used to load goals) ---
SCENARIOS = {
    "coffee_ordering": {
        "id": "coffee_ordering",
        "role": "waiter",
        "description": "Student practices ordering coffee...",
        "goals": ["greeting", "order_drink", "optionally_order_food", "confirm_order", "closing"]
    }
}

# --- 3. Helper Functions ---
def ensure_session_defaults(state: dict, request: ChatRequest):
    """Sets up the session memory if it is a new session."""
    if state.get("scenario") is None:
        state["scenario"] = request.scenario_id
        
        # Load goals based on scenario ID
        scenario_def = SCENARIOS.get(request.scenario_id, {})
        state["goals_to_complete"] = list(scenario_def.get("goals", []))
        state["all_goals_completed"] = False
        
        # Lists to store our separated data
        state["llm_responses"] = []
        state["evaluations"] = []
        
    if state.get("native_language") is None:
        state["native_language"] = request.native_language

    if state.get("target_language") is None:
        state["target_language"] = request.target_language

def get_valid_llm_response(prompt: str, max_retries: int = 2):
    """Handles the LLM calling and JSON parsing logic."""
    for _ in range(max_retries):
        llm_raw = generate_chat_response(prompt)
        print("RAW LLM OUTPUT:\n", llm_raw)

        llm_output = parse_llm_json(llm_raw)
        if llm_output:
            return llm_output, llm_raw
            
    print("FAILED TO PARSE JSON AFTER RETRIES")
    return None, llm_raw

# --- 4. API Endpoints ---
@app.get("/")
def root():
    return {"status": "LanguageCafe backend running"}

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

@app.post("/chat")
def chat(request: ChatRequest):
    state = get_session(request.session_id)

    # 1. Prepare session
    ensure_session_defaults(state, request)

    # 2. Get LLM Response
    prompt = create_prompt(state, request.message)
    llm_output, llm_raw = get_valid_llm_response(prompt)

    if llm_output is None:
        return {"response": "Sorry, something went wrong. Please try again."}

    # 3. Save memory aspects separately
    state["llm_responses"].append(llm_output)
    
    state["evaluations"].append({
        "communicative_success": llm_output.get("communicative_success"),
        "detected_goal": llm_output.get("detected_goal"),
        "corrections": llm_output.get("corrections", [])
    })

    bot_response = llm_output.get("response", "Sorry, I couldn't generate a response.")
    
    state["chat_history"].append({"role": "student", "content": request.message})
    state["chat_history"].append({"role": "assistant", "content": bot_response})

    # 4. Update Goals
    update_goals(state, llm_output)
    
    if len(state.get("goals_to_complete", [])) == 0:
        state["all_goals_completed"] = True

    # 5. Return ONLY the string response
    return {"response": bot_response}

# --- 5. Data Retrieval Endpoints ---
@app.get("/session/{session_id}/goals")
def get_session_goals(session_id: str):
    state = get_session(session_id)
    if not state:
        raise HTTPException(status_code=404, detail="Session not found")
        
    return {
        "session_id": session_id,
        "goals_to_complete": state.get("goals_to_complete", []),
        "all_goals_completed": state.get("all_goals_completed", False)
    }

@app.get("/session/{session_id}/responses")
def get_session_responses(session_id: str):
    state = get_session(session_id)
    if not state:
        raise HTTPException(status_code=404, detail="Session not found")
        
    return {
        "session_id": session_id,
        "llm_responses": state.get("llm_responses", [])
    }

@app.get("/session/{session_id}/history")
def get_chat_history(session_id: str):
    state = get_session(session_id)
    if not state:
        raise HTTPException(status_code=404, detail="Session not found")
        
    return {
        "session_id": session_id,
        "chat_history": state.get("chat_history", [])
    }
