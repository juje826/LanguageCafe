from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()

class ChatRequest(BaseModel):
    message: str

@app.get("/")
def root():
    return {"status": "LanguageCafe backend running"}

@app.post("/chat")
def chat(request: ChatRequest):
    user_message = request.message

    # temporary response
    response = f"You said: {user_message}"

    return {"response": response}