from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import ShoppingListItem
from app.schemas import ShoppingListItemCreate, ShoppingListItemOut, ShoppingListItemPatch

router = APIRouter(prefix="/shopping-list", tags=["shopping-list"])


@router.get("", response_model=list[ShoppingListItemOut])
def get_shopping_list(db: Session = Depends(get_db)):
    return db.query(ShoppingListItem).all()


@router.post("", response_model=ShoppingListItemOut, status_code=201)
def add_shopping_item(payload: ShoppingListItemCreate, db: Session = Depends(get_db)):
    item = ShoppingListItem(**payload.model_dump())
    db.add(item)
    db.commit()
    db.refresh(item)
    return item


@router.patch("/{item_id}", response_model=ShoppingListItemOut)
def patch_shopping_item(item_id: int, payload: ShoppingListItemPatch, db: Session = Depends(get_db)):
    item = db.get(ShoppingListItem, item_id)
    if not item:
        raise HTTPException(status_code=404, detail="Shopping list item not found")
    for field, value in payload.model_dump(exclude_unset=True).items():
        setattr(item, field, value)
    db.commit()
    db.refresh(item)
    return item
