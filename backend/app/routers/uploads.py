import uuid
from pathlib import Path

from fastapi import APIRouter, Depends, HTTPException, Query, UploadFile, File
from sqlalchemy.orm import Session

from app.config import DATA_DIR
from app.database import get_db
from app.models import LocationEnum, UploadEvent, UploadTypeEnum, Receipt
from app.schemas import UploadEventOut

router = APIRouter(prefix="/uploads", tags=["uploads"])


def _save_file(upload: UploadFile, subdir: str) -> str:
    """Persist uploaded file to DATA_DIR/<subdir>/ and return the filename."""
    dest_dir: Path = DATA_DIR / subdir
    dest_dir.mkdir(parents=True, exist_ok=True)

    suffix = Path(upload.filename or "image").suffix or ".jpg"
    filename = f"{uuid.uuid4().hex}{suffix}"
    dest_path = dest_dir / filename

    content = upload.file.read()
    dest_path.write_bytes(content)
    return filename


@router.post("/inventory-photo", response_model=UploadEventOut, status_code=201)
def upload_inventory_photo(
    location: LocationEnum = Query(..., description="Storage location: FRIDGE | FREEZER | PANTRY"),
    file: UploadFile = File(...),
    db: Session = Depends(get_db),
):
    filename = _save_file(file, "inventory")
    event = UploadEvent(
        type=UploadTypeEnum.inventory_photo,
        location=location,
        filename=filename,
    )
    db.add(event)
    db.commit()
    db.refresh(event)
    return event


@router.post("/receipt-photo", response_model=UploadEventOut, status_code=201)
def upload_receipt_photo(
    file: UploadFile = File(...),
    db: Session = Depends(get_db),
):
    filename = _save_file(file, "receipts")

    # Automatically create a Receipt record for this upload.
    receipt = Receipt(image_filename=filename)
    db.add(receipt)

    event = UploadEvent(
        type=UploadTypeEnum.receipt_photo,
        location=None,
        filename=filename,
    )
    db.add(event)
    db.commit()
    db.refresh(event)
    return event
