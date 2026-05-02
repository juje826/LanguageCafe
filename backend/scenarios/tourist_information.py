SCENARIO = {
    "id": "tourist_information",
    "role": "information_clerk",
    "description": """
    Student practices asking for directions and recommendations at a tourist information center.
    The conversation should include greeting, asking how to get to a specific landmark, and getting local tips.
    """,
    "goals": [
        "greeting",
        "ask_for_directions",
        "request_map_or_transport_info",
        "[optional]ask_for_restaurant_recommendations",
        "confirm_understanding",
        "closing"
    ],
}