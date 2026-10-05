"""Routes for checking the service's health."""

from fastapi import APIRouter
from sqlalchemy import text
from sqlalchemy.orm import Session
from fastapi import Depends

from app.database import get_db

router = APIRouter(tags=["health"])


@router.get("/health")
def health(db: Session = Depends(get_db)):
    try:
        db.execute(text("SELECT 1"))
        return {"status": "ok"}
    except Exception:
        return JSONResponse(status_code=503, content={"status": "unavailable"})