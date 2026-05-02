SCENARIO = {
    "id": "hotel_check_in",
    "role": "receptionist",
    "description": """
    Student practices checking into a hotel at the front desk.
    The conversation should include providing reservation details, showing ID, and receiving the room key.
    """,
    "goals": [
        "greeting",
        "provide_reservation_name",
        "present_id_or_credit_card",
        "[optional]ask_about_breakfast_or_amenities",
        "receive_room_key",
        "closing"
    ],
}