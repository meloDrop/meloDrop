from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.genres import Genre

router = APIRouter(prefix="/genres", tags=["genres"])


@router.get("")
def get_genres(db: Session = Depends(get_db)):
    genres = db.query(Genre).all()
    return genres