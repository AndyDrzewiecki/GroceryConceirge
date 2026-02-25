"""
Inventory service – helper functions for querying and summarising inventory.

Designed to be the single point of truth for inventory data access so that
future AI-driven features can call these helpers instead of raw SQL.
"""

from datetime import date, timedelta
from typing import Sequence

from sqlalchemy.orm import Session

from app.models import InventoryItem, LocationEnum


def get_all_items(db: Session) -> Sequence[InventoryItem]:
    return db.query(InventoryItem).all()


def get_items_by_location(db: Session, location: LocationEnum) -> Sequence[InventoryItem]:
    return db.query(InventoryItem).filter(InventoryItem.location == location).all()


def get_expiring_soon(db: Session, days: int = 3) -> Sequence[InventoryItem]:
    """Return items expiring within *days* calendar days."""
    cutoff = date.today() + timedelta(days=days)
    return (
        db.query(InventoryItem)
        .filter(InventoryItem.expiry_date != None, InventoryItem.expiry_date <= cutoff)
        .all()
    )


def item_names_in_inventory(db: Session) -> set[str]:
    """Return a lowercase set of item names currently in inventory."""
    rows = db.query(InventoryItem.name).all()
    return {r[0].lower() for r in rows}
