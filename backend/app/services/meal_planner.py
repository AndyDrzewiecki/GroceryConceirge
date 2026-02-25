"""
Meal planner service – v1 deterministic placeholder.

Generates a 7-day meal plan based on the current inventory.  The logic here is
intentionally simple so it can be replaced with an AI call (e.g. OpenAI) later
by just swapping out the _plan_meals() function.

Extension point
---------------
To add AI-based planning, replace _plan_meals() with a function that calls the
OpenAI chat-completions API and returns the same list[tuple[date, str, str]]
shape.  The rest of the service (DB persistence, shopping list generation) stays
the same.
"""

from datetime import date, timedelta
from typing import Sequence

from sqlalchemy.orm import Session

from app.models import InventoryItem, Meal, MealPlan
from app.services.inventory_service import get_all_items
from app.services.shopping_list_generator import replenish_shopping_list

# ── Deterministic meal templates ──────────────────────────────────────────────
# Each entry is (meal_name, required_ingredients).
_MEAL_TEMPLATES: list[tuple[str, list[str]]] = [
    ("Spaghetti Bolognese", ["spaghetti", "ground beef", "tomato sauce", "onion", "garlic"]),
    ("Grilled Chicken & Rice", ["chicken breast", "rice", "olive oil", "lemon"]),
    ("Vegetable Stir Fry", ["broccoli", "carrot", "bell pepper", "soy sauce", "garlic", "rice"]),
    ("Omelette & Toast", ["eggs", "butter", "bread", "milk"]),
    ("Lentil Soup", ["lentils", "onion", "carrot", "celery", "tomato", "cumin"]),
    ("Tuna Pasta", ["pasta", "tuna", "olive oil", "garlic", "lemon", "capers"]),
    ("Pancakes", ["flour", "eggs", "milk", "butter", "maple syrup"]),
]


def _plan_meals(
    inventory: Sequence[InventoryItem],
    week_start: date,
) -> list[tuple[date, str, list[str]]]:
    """
    Return a list of (meal_date, meal_name, required_ingredients) for 7 days.

    v1: cycles through _MEAL_TEMPLATES deterministically.
    Future v2: replace this function body with an AI call.
    """
    plan: list[tuple[date, str, list[str]]] = []
    for i in range(7):
        template = _MEAL_TEMPLATES[i % len(_MEAL_TEMPLATES)]
        meal_date = week_start + timedelta(days=i)
        plan.append((meal_date, template[0], template[1]))
    return plan


def generate_meal_plan(db: Session, week_start: date) -> MealPlan:
    """
    Generate (or regenerate) a MealPlan for the week starting on *week_start*.

    Steps:
    1. Delete any existing meal plan that starts on the same week.
    2. Build meals from inventory.
    3. Persist MealPlan + Meal rows.
    4. Call shopping list generator for missing ingredients.
    5. Return the saved MealPlan.
    """
    # Remove existing plan for this week (idempotent regeneration).
    existing = db.query(MealPlan).filter(MealPlan.week_start_date == week_start).first()
    if existing:
        db.delete(existing)
        db.commit()

    inventory = get_all_items(db)
    planned_days = _plan_meals(inventory, week_start)

    meal_plan = MealPlan(week_start_date=week_start)
    db.add(meal_plan)
    db.flush()  # get meal_plan.id

    all_ingredients: list[str] = []
    for meal_date, meal_name, ingredients in planned_days:
        meal = Meal(
            meal_plan_id=meal_plan.id,
            date=meal_date,
            meal_name=meal_name,
            notes=", ".join(ingredients),
        )
        db.add(meal)
        all_ingredients.extend(ingredients)

    db.commit()
    db.refresh(meal_plan)

    # Populate shopping list with any missing ingredients.
    replenish_shopping_list(db, all_ingredients)

    return meal_plan
