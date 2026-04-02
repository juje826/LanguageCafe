from .coffee_ordering import SCENARIO
from .prompt_builder import build_prompt

SCENARIOS = {
    "coffee_ordering": SCENARIO
}

def create_prompt(state, user_message):
    scenario = SCENARIOS[state["scenario"]]
    return build_prompt(state, scenario, user_message)

def update_goals(state, llm_output):
    goal = llm_output.get("detected_goal")

    if goal is None:
        return

    if goal not in state["goals_achieved"]:
        state["goals_achieved"].append(goal)