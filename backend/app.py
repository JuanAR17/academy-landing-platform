import os
import json
import csv
import datetime as dt
import unicodedata
from pathlib import Path
from typing import Literal

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, EmailStr, field_validator

# SQLAlchemy async
from sqlalchemy import String, Text, DateTime, Integer
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column
from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker

app = FastAPI()

# ====== Config ======
# CORS desde variables de entorno (p.ej., https://academy.tu-dominio.com)
CORS_ALLOWED = [o.strip() for o in os.getenv("CORS_ALLOWED_ORIGINS", "").split(",") if o.strip()]
if CORS_ALLOWED:
    app.add_middleware(
        CORSMiddleware,
        allow_origins=CORS_ALLOWED,
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

# DB URL del docker-compose: postgresql+asyncpg://user:pass@postgres:5432/db
DATABASE_URL = os.getenv("DATABASE_URL", "").replace("postgresql://", "postgresql+asyncpg://")

# ===== CSV (se mantiene por compatibilidad, pero no se usará en prod) =====
CSV_PATH: Path = Path("data.csv")
FIELDNAMES: list[str] = ["ts_utc", "email", "name", "courses_json"]

# ---- Allowed course catalog (ajústalo a tus nombres) ----
Course = Literal["ai_fundamentals", "ml_advanced", "dl_bootcamp"]

_FORMULA_PREFIXES = ("=", "+", "-", "@")

def _neutralize_formula(cell: str) -> str:
    return "'" + cell if cell.startswith(_FORMULA_PREFIXES) else cell

def _strip_controls(cell: str) -> str:
    if "\x00" in cell:
        cell = cell.replace("\x00", "")
    cell = cell.replace("\r", " ").replace("\n", " ")
    cell = "".join((" " if (unicodedata.category(ch) == "Cc" and ch != "\t") else ch) for ch in cell)
    return " ".join(cell.split())

def safe_text(cell: str) -> str:
    return _neutralize_formula(_strip_controls(cell))

# ===== Pydantic model =====
class Lead(BaseModel):
    email: EmailStr
    name: str
    courses: set[Course]

    @field_validator("name")
    @classmethod
    def name_not_empty(cls, v: str) -> str:
        s = v.strip()
        if not s:
            raise ValueError("name must be non-empty")
        return s

    @field_validator("courses")
    @classmethod
    def courses_nonempty(cls, v: set[Course]) -> set[Course]:
        if not v:
            raise ValueError("courses must be a non-empty set")
        return v

# ===== SQLAlchemy ORM =====
class Base(DeclarativeBase):
    pass

class LeadORM(Base):
    __tablename__ = "leads"
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    ts_utc: Mapped[dt.datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    email: Mapped[str] = mapped_column(String(320), nullable=False, index=True)
    name: Mapped[str] = mapped_column(String(255), nullable=False)
    courses_json: Mapped[str] = mapped_column(Text, nullable=False)

engine = create_async_engine(DATABASE_URL, pool_pre_ping=True, echo=False) if DATABASE_URL else None
Session = async_sessionmaker(engine, expire_on_commit=False) if engine else None

@app.on_event("startup")
async def on_startup() -> None:
    # Si hay DB, crea la tabla si no existe; si no hay DB, crea CSV con header.
    if engine:
        async with engine.begin() as conn:
            await conn.run_sync(Base.metadata.create_all)
    else:
        if (not CSV_PATH.exists()) or CSV_PATH.stat().st_size == 0:
            with CSV_PATH.open("w", newline="", encoding="utf-8-sig") as f:
                w = csv.DictWriter(f, fieldnames=FIELDNAMES, dialect="excel",
                                   delimiter=",", quotechar='"', lineterminator="\n",
                                   quoting=csv.QUOTE_MINIMAL, doublequote=True, escapechar=None)
                w.writeheader()

@app.get("/health")
async def health():
    return {"status": "ok"}

@app.post("/ingest")
async def ingest(lead: Lead):
    """Guarda un Lead en Postgres (si DATABASE_URL está presente); de lo contrario, en CSV."""
    ts = dt.datetime.now(dt.UTC)
    courses_sorted_list = sorted(lead.courses)

    if Session:
        async with Session() as session:
            obj = LeadORM(
                ts_utc=ts,
                email=safe_text(str(lead.email)),
                name=safe_text(lead.name),
                courses_json=json.dumps(courses_sorted_list, ensure_ascii=False),
            )
            session.add(obj)
            await session.commit()
            await session.refresh(obj)
            return {"ok": True, "id": obj.id}
    else:
        row = {
            "ts_utc": ts.strftime("%Y-%m-%dT%H:%M:%SZ"),
            "email": safe_text(str(lead.email)),
            "name": safe_text(lead.name),
            "courses_json": json.dumps(courses_sorted_list, ensure_ascii=False),
        }
        with CSV_PATH.open("a", newline="", encoding="utf-8") as f:
            w = csv.DictWriter(f, fieldnames=FIELDNAMES, dialect="excel",
                               delimiter=",", quotechar='"', lineterminator="\n",
                               quoting=csv.QUOTE_MINIMAL, doublequote=True, escapechar=None)
            w.writerow(row)
        return {"ok": True, "rows_appended": 1}
