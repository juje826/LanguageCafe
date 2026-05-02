from .coffee_ordering import SCENARIO
from .doctor_visit import SCENARIO
from .hotel_check_in import SCENARIO
from .job_interview import SCENARIO
from .store_return import SCENARIO
from .tourist_information import SCENARIO
from .prompt_builder import build_prompt

SCENARIOS = {
    "coffee_ordering": SCENARIO,
    "doctor_visit": SCENARIO,
    "hotel_check_in": SCENARIO,
    "job_interview": SCENARIO,
    "store_return": SCENARIO,
    "tourist_information": SCENARIO,
}

def create_prompt(state, user_message):
    scenario = SCENARIOS[state["scenario"]]
    return build_prompt(state, scenario, user_message)

def update_goals(state, llm_output):
    goal = llm_output.get("detected_goal")

    if goal is None:
        return

    remaining_goals = state.get("goals_to_complete", [])

    if goal in remaining_goals:
        remaining_goals.remove(goal)

    # Check completion
    if all(goal.startswith("[optional]") for goal in remaining_goals):
        state["all_goals_completed"] = True