from datetime import date, datetime

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import func

from app.database import get_db
from app.models import Receipt, MealPlan, Meal, ShoppingListItem
from app.schemas import DashboardOut

router = APIRouter(prefix="/dashboard", tags=["dashboard"])


@router.get("", response_model=DashboardOut)
def get_dashboard(db: Session = Depends(get_db)):
    today = date.today()

    # Month grocery spend – sum of receipt totals for current calendar month.
    month_start = today.replace(day=1)
    month_spend: float = db.query(func.coalesce(func.sum(Receipt.total_amount), 0)).filter(
        Receipt.purchase_date >= month_start,
        Receipt.purchase_date <= today,
    ).scalar() or 0.0

    # Today's meal – look up the current meal plan and find today's meal.
    todays_meal: str | None = None
    meal = (
        db.query(Meal)
        .filter(Meal.date == today)
        .order_by(Meal.id.desc())
        .first()
    )
    if meal:
        todays_meal = meal.meal_name

    # Shopping needed – any unpurchased items?
    unpurchased_count = db.query(func.count(ShoppingListItem.id)).filter(
        ShoppingListItem.purchased == False
    ).scalar() or 0
    shopping_needed = unpurchased_count > 0

    return DashboardOut(
        month_grocery_spend=float(month_spend),
        todays_meal=todays_meal,
        shopping_needed=shopping_needed,
    )
