import csv
import datetime as dt
import json
import unicodedata
from pathlib import Path
from typing import Literal

from fastapi import FastAPI
from pydantic import BaseModel, EmailStr, field_validator

app = FastAPI()

# ----- Config -----
CSV_PATH: Path = Path("data.csv")
FIELDNAMES: list[str] = ["ts_utc", "email", "name", "courses_json"]

# ---- Allowed course catalog (edit these 3 literals to your exact names) ----
Course = Literal["ai_fundamentals", "ml_advanced", "dl_bootcamp"]


# ===== CSV safety helpers =====================================================
_FORMULA_PREFIXES = ("=", "+", "-", "@")


def _neutralize_formula(cell: str) -> str:
    # Prevent CSV formulas from executing in Excel/Sheets by prefixing with "'"
    return "'" + cell if cell.startswith(_FORMULA_PREFIXES) else cell


def _strip_controls(cell: str) -> str:
    # Remove NUL; replace CR/LF and other control chars with spaces; squash runs
    if "\x00" in cell:
        cell = cell.replace("\x00", "")
    cell = cell.replace("\r", " ").replace("\n", " ")
    cell = "".join(
        (" " if (unicodedata.category(ch) == "Cc" and ch != "\t") else ch)
        for ch in cell
    )
    return " ".join(cell.split())


def safe_text(cell: str) -> str:
    # Compose sanitizers
    return _neutralize_formula(_strip_controls(cell))


# ===== Model ==================================================================
class Lead(BaseModel):
    email: EmailStr
    name: str
    courses: set[Course]  # set of restricted string literals

    @field_validator("name")
    @classmethod
    def name_not_empty(cls, v: str) -> str:
        s = v.strip()
        if not s:
            msg = "name must be non-empty"
            raise ValueError(msg)
        return s

    @field_validator("courses")
    @classmethod
    def courses_nonempty(cls, v: set[Course]) -> set[Course]:
        if not v:
            msg = "courses must be a non-empty set"
            raise ValueError(msg)
        return v


# ===== CSV helpers ============================================================
def ensure_csv_with_header(path: Path) -> None:
    # If creating from scratch or empty, write header with UTF-8 BOM
    # so Excel autodetects encoding correctly.
    if (not path.exists()) or path.stat().st_size == 0:
        with path.open("w", newline="", encoding="utf-8-sig") as f:
            w = csv.DictWriter(
                f,
                fieldnames=FIELDNAMES,
                dialect="excel",
                delimiter=",",
                quotechar='"',
                lineterminator="\n",
                quoting=csv.QUOTE_MINIMAL,
                doublequote=True,
                escapechar=None,
            )
            w.writeheader()


ensure_csv_with_header(CSV_PATH)


# ===== Route (sync; avoids async-blocking lint) ===============================
@app.post("/ingest")
def ingest(lead: Lead):
    """Append a validated Lead to data.csv."""
    ts = dt.datetime.now(dt.UTC).strftime("%Y-%m-%dT%H:%M:%SZ")

    # Deterministic order for set (for stable CSV diffs)
    courses_sorted_list = sorted(lead.courses)

    row = {
        "ts_utc": ts,
        "email": safe_text(str(lead.email)),
        "name": safe_text(lead.name),
        # Store list as JSON string to avoid delimiter issues in CSV cells
        "courses_json": json.dumps(courses_sorted_list, ensure_ascii=False),
    }

    # Append without BOM; only header used BOM on first creation
    with CSV_PATH.open("a", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(
            f,
            fieldnames=FIELDNAMES,
            dialect="excel",
            delimiter=",",
            quotechar='"',
            lineterminator="\n",
            quoting=csv.QUOTE_MINIMAL,
            doublequote=True,
            escapechar=None,
        )
        w.writerow(row)

    return {"ok": True, "rows_appended": 1, "data": row}
