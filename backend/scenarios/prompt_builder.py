def build_prompt(state, scenario, user_message):
    native_lang = state["native_language"]
    target_lang = state["target_language"]
    history_text = format_history(state["chat_history"])
    remaining_goals = state.get("goals_to_complete", [])
    current_goal = remaining_goals[0] if remaining_goals else "All goals completed"

    goals_text = "\n".join(f"- {g}" for g in remaining_goals)

    prompt = f"""
    ROLE:
    You are a {scenario["role"]} in a roleplay scenario.
    
    LANGUAGE SETTINGS:
    - The student's native language is: {native_lang}
    - The student is learning: {target_lang}

    SCENARIO GOALS (remaining):
    {goals_text}

    CURRENT GOAL:
    {current_goal}

    CONVERSATION SO FAR:
    {history_text}

    STUDENT MESSAGE:
    {user_message}

    TASK:
    - Continue the roleplay naturally
    - Guide the student toward completing the CURRENT GOAL
    - Stay in character at all times
    - Speak primarily in {target_lang}
    - Only use {native_lang} if absolutely necessary for clarity

    LANGUAGE EVALUATION:
    - Identify mistakes in grammar, spelling, vocabulary, or language usage
    - The student MUST speak in {target_lang}
    - If the student uses another language, ALWAYS correct it
    - Only include corrections if there are actual mistakes
    - Be concise and helpful

    OUTPUT FORMAT:
    Return ONLY JSON with the following structure:

    {{
    "communicative_success": true or false,
    "detected_goal": "one of: {', '.join(scenario["goals"])} or null",
    "corrections": [
        {{
        "original": "incorrect part",
        "corrected": "correct version",
        "explanation": "short explanation"
        }}
    ],
    "response": "roleplay reply in {target_lang}",
    "translation": "full translation of the response in {native_lang}"
    }}

    IMPORTANT:
    - Do not include text outside JSON
    - If no mistakes: return an empty corrections list []
    - detected_goal must match EXACTLY one of the listed goals or be null
    """

    return prompt

def format_history(history):
    if len(history) == 0:
        return "Conversation just started."

    lines = []

    for msg in history:
        role = msg["role"]
        content = msg.get("text", "")
        lines.append(f"{role}: {content}")

    return "\n".join(lines)
