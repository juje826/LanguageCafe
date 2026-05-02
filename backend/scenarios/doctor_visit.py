SCENARIO = {
    "id": "doctor_visit",
    "role": "doctor",
    "description": """
    Student practices describing symptoms to a doctor during a medical appointment.
    The conversation should include greeting, explaining the medical issue, answering questions, and receiving advice.
    """,
    "goals": [
        "greeting",
        "describe_symptoms",
        "answer_follow_up_questions",
        "[optional]ask_about_medication_side_effects",
        "receive_diagnosis_or_prescription",
        "closing"
    ],
}