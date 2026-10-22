"""Routes of the profile module."""

from fastapi import APIRouter, Depends, HTTPException, Path
from sqlalchemy.orm import Session

from app.database import get_db
from app.dependencies import current_user
from app.exceptions import UserNotFoundError
from app.schemas import profile as schemas
from app.services import profile as services

# Every route of this module needs a logged-in user
router = APIRouter(prefix="/users", tags=["profile"], dependencies=[Depends(current_user)])


@router.get(
    "/{user_id}/profile",
    response_model=schemas.ProfileOut,
    responses={
        200: {"description": "The profile, without private data"},
        404: {"description": "No user has this id"},
    },
)
def get_profile(
    user_id: int = Path(description="meloDrop user id (id in GET /me, author_id in GET /feed)"),
    db: Session = Depends(get_db),
):
    """Get a user's profile: counters, drop of the day and 5 last drops."""
    try:
        return services.get_profile(db, user_id)
    except UserNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))