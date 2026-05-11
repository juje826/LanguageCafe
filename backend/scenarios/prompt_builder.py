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

    LANGUAGE EVALUATION (for corrections):
    - Evaluate the student's message carefully
    - The student MUST write in {target_lang}
    - If ANY word is not in {target_lang}, include a correction
    - ALWAYS detect:
        - wrong language
        - grammar mistakes
        - spelling mistakes
        - incorrect vocabulary
    - Do NOT ignore mistakes, even if the meaning is understandable
    - If the student's message is fully correct, return []
    - Explanations must be short and written in {native_lang}
    - Be concise and helpful

    CORRECTION EXAMPLES:

    Example 1:
    Target language: Spanish
    Student message:
    "bonjour monsieur"

    Corrections:
    [
      {
        "original": "bonjour monsieur",
        "corrected": "hola señor",
        "explanation": "Use Spanish instead of French."
      }
    ]

    Example 2:
    Target language: Spanish
    Student message:
    "yo querer cafe"

    Corrections:
    [
      {
        "original": "yo querer cafe",
        "corrected": "yo quiero café",
        "explanation": "Use the correct verb conjugation and spelling."
      }
    ]

    Example 3:
    Target language: Spanish
    Student message:
    "quiero un coffee"

    Corrections:
    [
      {
        "original": "coffee",
        "corrected": "café",
        "explanation": "Use Spanish vocabulary."
      }
    ]


    OUTPUT FORMAT:
    Return ONLY JSON with the following structure:

    {{
    "communicative_success": true or false,
    "detected_goal": "one of: {', '.join(scenario["goals"])} or null",
    "corrections": [
        {{
        "original": "incorrect part",
        "corrected": "correct version",
        "explanation": "short explanation in {native_lang}"
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
