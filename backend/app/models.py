import enum
from datetime import datetime, date

from sqlalchemy import (
    Boolean,
    Column,
    Date,
    DateTime,
    Enum,
    Float,
    ForeignKey,
    Integer,
    String,
    Text,
    func,
)
from sqlalchemy.orm import relationship

from app.database import Base


class LocationEnum(str, enum.Enum):
    FRIDGE = "FRIDGE"
    FREEZER = "FREEZER"
    PANTRY = "PANTRY"


class UploadTypeEnum(str, enum.Enum):
    inventory_photo = "inventory_photo"
    receipt_photo = "receipt_photo"


class InventoryItem(Base):
    __tablename__ = "inventory_item"

    id = Column(Integer, primary_key=True, index=True)
    location = Column(Enum(LocationEnum), nullable=False)
    name = Column(String(255), nullable=False)
    quantity = Column(Float, nullable=False, default=1.0)
    unit = Column(String(64), nullable=False, default="unit")
    expiry_date = Column(Date, nullable=True)
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now(), nullable=False)


class UploadEvent(Base):
    __tablename__ = "upload_event"

    id = Column(Integer, primary_key=True, index=True)
    type = Column(Enum(UploadTypeEnum), nullable=False)
    location = Column(Enum(LocationEnum), nullable=True)
    filename = Column(String(512), nullable=False)
    created_at = Column(DateTime, default=func.now(), nullable=False)


class Receipt(Base):
    __tablename__ = "receipt"

    id = Column(Integer, primary_key=True, index=True)
    store = Column(String(255), nullable=True)
    total_amount = Column(Float, nullable=True)
    purchase_date = Column(Date, nullable=True)
    image_filename = Column(String(512), nullable=False)
    created_at = Column(DateTime, default=func.now(), nullable=False)


class MealPlan(Base):
    __tablename__ = "meal_plan"

    id = Column(Integer, primary_key=True, index=True)
    week_start_date = Column(Date, nullable=False)
    created_at = Column(DateTime, default=func.now(), nullable=False)
    meals = relationship("Meal", back_populates="meal_plan", cascade="all, delete-orphan")


class Meal(Base):
    __tablename__ = "meal"

    id = Column(Integer, primary_key=True, index=True)
    meal_plan_id = Column(Integer, ForeignKey("meal_plan.id"), nullable=False)
    date = Column(Date, nullable=False)
    meal_name = Column(String(255), nullable=False)
    notes = Column(Text, nullable=True)
    meal_plan = relationship("MealPlan", back_populates="meals")


class ShoppingListItem(Base):
    __tablename__ = "shopping_list_item"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), nullable=False)
    quantity = Column(Float, nullable=False, default=1.0)
    unit = Column(String(64), nullable=False, default="unit")
    purchased = Column(Boolean, default=False, nullable=False)
