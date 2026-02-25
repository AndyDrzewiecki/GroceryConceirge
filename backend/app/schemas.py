from __future__ import annotations

from datetime import date, datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict

from app.models import LocationEnum, UploadTypeEnum


# ── Inventory ──────────────────────────────────────────────────────────────────

class InventoryItemCreate(BaseModel):
    location: LocationEnum
    name: str
    quantity: float = 1.0
    unit: str = "unit"
    expiry_date: Optional[date] = None


class InventoryItemUpdate(BaseModel):
    location: Optional[LocationEnum] = None
    name: Optional[str] = None
    quantity: Optional[float] = None
    unit: Optional[str] = None
    expiry_date: Optional[date] = None


class InventoryItemOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    location: LocationEnum
    name: str
    quantity: float
    unit: str
    expiry_date: Optional[date]
    updated_at: datetime


# ── Upload Events ──────────────────────────────────────────────────────────────

class UploadEventOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    type: UploadTypeEnum
    location: Optional[LocationEnum]
    filename: str
    created_at: datetime


# ── Receipts ───────────────────────────────────────────────────────────────────

class ReceiptMetadataUpdate(BaseModel):
    store: Optional[str] = None
    total_amount: Optional[float] = None
    purchase_date: Optional[date] = None


class ReceiptOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    store: Optional[str]
    total_amount: Optional[float]
    purchase_date: Optional[date]
    image_filename: str
    created_at: datetime


# ── Shopping List ──────────────────────────────────────────────────────────────

class ShoppingListItemCreate(BaseModel):
    name: str
    quantity: float = 1.0
    unit: str = "unit"


class ShoppingListItemPatch(BaseModel):
    purchased: Optional[bool] = None
    name: Optional[str] = None
    quantity: Optional[float] = None
    unit: Optional[str] = None


class ShoppingListItemOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    name: str
    quantity: float
    unit: str
    purchased: bool


# ── Meal Plan ─────────────────────────────────────────────────────────────────

class MealOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    date: date
    meal_name: str
    notes: Optional[str]


class MealPlanOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    week_start_date: date
    created_at: datetime
    meals: list[MealOut]


# ── Dashboard ─────────────────────────────────────────────────────────────────

class DashboardOut(BaseModel):
    month_grocery_spend: float
    todays_meal: Optional[str]
    shopping_needed: bool
