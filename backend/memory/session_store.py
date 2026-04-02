sessions = {}

def get_session(session_id):

    if session_id not in sessions:

        sessions[session_id] = {
            "scenario": None,
            "level": "A1",
            "chat_history": [],
            "goals_achieved": []
        }

    return sessions[session_id]