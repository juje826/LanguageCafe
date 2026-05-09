from . import coffee_ordering
from . import doctor_visit
from . import hotel_check_in
from . import job_interview
from . import store_return
from . import tourist_information
from .prompt_builder import build_prompt

SCENARIOS = {
    "coffee_ordering": coffee_ordering.SCENARIO,
    "doctor_visit": doctor_visit.SCENARIO,
    "hotel_check_in": hotel_check_in.SCENARIO,
    "job_interview": job_interview.SCENARIO,
    "store_return": store_return.SCENARIO,
    "tourist_information": tourist_information.SCENARIO,
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