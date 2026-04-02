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

@app.get("/")
def root():
    return {"status": "LanguageCafe backend running"}

@app.post("/chat")
def chat(request: ChatRequest):
    state = get_session(request.session_id)

    if state["scenario"] is None:
        state["scenario"] = "coffee_ordering"  # change later to request.scenario_id

    # Create prompt from user message
    user_message = request.message
    prompt = create_prompt(state, user_message)

    # parse JSON and try again if invalid JSON is returned
    MAX_RETRIES = 2
    for _ in range(MAX_RETRIES):
        llm_raw = generate_chat_response(prompt)
        llm_output = parse_llm_json(llm_raw)
        if llm_output:
            break

    # store conversation state
    state["chat_history"].append({
        "role": "student",
        "content": request.message
    })

    # update goals
    update_goals(state, llm_output)

    return llm_output
