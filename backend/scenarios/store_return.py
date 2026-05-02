SCENARIO = {
    "id": "store_return",
    "role": "customer_service_representative",
    "description": """
    Student practices returning a defective or unwanted item at a retail store.
    The conversation should include stating the reason for the return, providing the receipt, and processing the refund.
    """,
    "goals": [
        "greeting",
        "state_item_to_return",
        "explain_reason_for_return",
        "provide_receipt",
        "[optional]request_exchange_instead_of_refund",
        "confirm_refund_method",
        "closing"
    ],
}