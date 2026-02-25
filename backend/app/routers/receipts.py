from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import Receipt
from app.schemas import ReceiptMetadataUpdate, ReceiptOut

router = APIRouter(prefix="/receipts", tags=["receipts"])


@router.get("", response_model=list[ReceiptOut])
def list_receipts(db: Session = Depends(get_db)):
    return db.query(Receipt).order_by(Receipt.created_at.desc()).all()


@router.post("/{receipt_id}/metadata", response_model=ReceiptOut)
def update_receipt_metadata(receipt_id: int, payload: ReceiptMetadataUpdate, db: Session = Depends(get_db)):
    receipt = db.get(Receipt, receipt_id)
    if not receipt:
        raise HTTPException(status_code=404, detail="Receipt not found")
    for field, value in payload.model_dump(exclude_unset=True).items():
        setattr(receipt, field, value)
    db.commit()
    db.refresh(receipt)
    return receipt
