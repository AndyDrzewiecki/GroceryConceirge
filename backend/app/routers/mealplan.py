from datetime import date, timedelta

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import MealPlan
from app.schemas import MealPlanOut
from app.services.meal_planner import generate_meal_plan

router = APIRouter(prefix="/mealplan", tags=["mealplan"])


@router.post("/generate", response_model=MealPlanOut, status_code=201)
def generate(db: Session = Depends(get_db)):
    """Generate (or regenerate) a 7-day meal plan starting from this Monday."""
    today = date.today()
    week_start = today - timedelta(days=today.weekday())  # Monday
    meal_plan = generate_meal_plan(db, week_start)
    return meal_plan


@router.get("/current", response_model=MealPlanOut)
def current(db: Session = Depends(get_db)):
    """Return the most recently generated meal plan."""
    meal_plan = db.query(MealPlan).order_by(MealPlan.created_at.desc()).first()
    if not meal_plan:
        raise HTTPException(status_code=404, detail="No meal plan found. Call POST /mealplan/generate first.")
    return meal_plan
