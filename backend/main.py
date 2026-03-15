from fastapi import FastAPI
from pydantic import BaseModel
from llm_service import generate_chat_response

app = FastAPI()

class ChatRequest(BaseModel):
    message: str

@app.get("/")
def root():
    return {"status": "LanguageCafe backend running"}

@app.post("/chat")
def chat(request: ChatRequest):
    user_message = request.message

    # LLM response
    response = generate_chat_response(user_message)

    return {"response": response}