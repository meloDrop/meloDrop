"""Routes of the genres module."""

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.genre import Genre


from app.dependencies import current_user


router = APIRouter(prefix="/genres", tags=["genres"], dependencies=[Depends(current_user)])


@router.get("")
def get_genres(db: Session = Depends(get_db)):
    """Return the list of all genres."""
    genres = db.query(Genre).all()
    return genres
