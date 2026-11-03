"""Routes of the feed module."""

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.database import get_db
from app.dependencies import current_user
from app.models.user import User
from app.schemas import feed as schemas
from app.services import feed as services

router = APIRouter(prefix="/feed", tags=["feed"])


@router.get(
    "",
    response_model=list[schemas.FeedItem],
    responses={200: {"description": "Up to 10 drops to discover, or an empty list"}},
)
def get_feed(user: User = Depends(current_user), db: Session = Depends(get_db)):
    """Get a batch of drops the user has not seen yet."""
    return services.get_feed(db, user.id)