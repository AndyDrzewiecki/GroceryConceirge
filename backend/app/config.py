import os
from pathlib import Path

DATABASE_URL: str = os.getenv(
    "DATABASE_URL",
    "postgresql://homeops:homeops@db:5432/homeops_grocery",
)

DATA_DIR: Path = Path(os.getenv("DATA_DIR", "/app/data"))
DATA_DIR.mkdir(parents=True, exist_ok=True)

APP_HOST: str = os.getenv("APP_HOST", "0.0.0.0")
APP_PORT: int = int(os.getenv("APP_PORT", "8000"))

# CORS – allow all origins so LAN devices can reach the API.
CORS_ORIGINS: list[str] = ["*"]
