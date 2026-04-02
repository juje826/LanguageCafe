def build_prompt(state, scenario, user_message):
    history_text = format_history(state["chat_history"])

    goals_text = "\n".join(f"- {g}" for g in scenario["goals"])

    achieved_text = ", ".join(state["goals_achieved"])

    prompt = f"""
    ROLE:
    You are a {scenario["role"]} in a roleplay scenario.

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

    EVALUATE STUDENT LANGUAGE:

    identify mistakes in grammar, spelling or vocabulary

    OUTPUT FORMAT:

    Return ONLY JSON:

    {{
    "communicative_success": true or false,

    "detected_goal": "one of: {', '.join(scenario["goals"])}"

    "corrections": ["list of corrections"],

    "response": "roleplay reply"
    }}

    Do not include text outside JSON.

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
