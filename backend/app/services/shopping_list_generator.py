"""
Shopping list generator.

Takes a list of required ingredient names (strings) and the current inventory,
then creates ShoppingListItem rows for ingredients that are missing or depleted.

This layer is intentionally stateless so it can be called from both the meal
planner service and directly from the API if needed.
"""

from sqlalchemy.orm import Session

from app.models import ShoppingListItem
from app.services.inventory_service import item_names_in_inventory


def replenish_shopping_list(db: Session, required_ingredients: list[str]) -> list[ShoppingListItem]:
    """
    For each ingredient in *required_ingredients* that is NOT already in the
    current inventory, add it to the shopping list (if not already listed).

    Returns the list of newly created ShoppingListItem rows.
    """
    have: set[str] = item_names_in_inventory(db)

    # Also skip items already on the shopping list (unpurchased).
    already_listed: set[str] = {
        row.name.lower()
        for row in db.query(ShoppingListItem).filter(ShoppingListItem.purchased == False).all()
    }

    created: list[ShoppingListItem] = []
    for ingredient in required_ingredients:
        key = ingredient.lower()
        if key not in have and key not in already_listed:
            item = ShoppingListItem(name=ingredient, quantity=1.0, unit="unit")
            db.add(item)
            already_listed.add(key)
            created.append(item)

    db.commit()
    return created
