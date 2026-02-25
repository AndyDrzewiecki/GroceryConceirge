from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import CORS_ORIGINS
from app.database import engine
from app.models import Base
from app.routers import dashboard, inventory, mealplan, receipts, shopping, uploads

# Auto-create all tables on startup (no Alembic).
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="HomeOps Grocery API",
    description="Mobile-first grocery management backend.",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── Routers ───────────────────────────────────────────────────────────────────
app.include_router(uploads.router)
app.include_router(inventory.router)
app.include_router(receipts.router)
app.include_router(dashboard.router)
app.include_router(shopping.router)
app.include_router(mealplan.router)


# ── Health ────────────────────────────────────────────────────────────────────
@app.get("/health", tags=["health"])
def health():
    return {"status": "ok"}
