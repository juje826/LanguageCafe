def build_prompt(state, scenario, user_message):
    native_lang = state["native_language"]
    target_lang = state["target_language"]
    history_text = format_history(state["chat_history"])
    goals_text = "\n".join(f"- {g}" for g in scenario["goals"])
    achieved_text = ", ".join(state["goals_achieved"])

    prompt = f"""
    ROLE:
    You are a {scenario["role"]} in a roleplay scenario.
    
    LANGUAGE SETTINGS:
    - The student's native language is: {native_lang}
    - The student is learning: {target_lang}

    COMMUNICATIVE GOALS:
    {goals_text}

    CONVERSATION SO FAR:
    {history_text}

    STUDENT MESSAGE:
    {user_message}

    GOALS ALREADY ACHIEVED:
    {achieved_text}

    TASK:
    - Continue the roleplay naturally
    - Guide conversation toward scenario goals
    - Adapt to unexpected input if needed
    - Talk primarily in the {target_lang} language: only if there are major misunderstandings or corrections, talk in {native_lang}

    EVALUATE STUDENT LANGUAGE:
    identify mistakes in grammar, spelling or vocabulary

    OUTPUT FORMAT:
    Return ONLY JSON with the following structure:

    {{
    "communicative_success": true or false,
    "detected_goal": "one of: {', '.join(scenario["goals"])}"
    "corrections": ["list of corrections"],
    "response": "roleplay reply"
    }}

    IMPORTANT:
    - Do not include text outside JSON.
    """

    return prompt

def format_history(history):

    if len(history) == 0:
        return "conversation only just started"

    lines = []

    for msg in history:

        role = msg["role"]

        content = msg["content"]

        lines.append(f"{role}: {content}")

    return "\n".join(lines)
