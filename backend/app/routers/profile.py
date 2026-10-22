"""Routes of the profile module."""

from fastapi import APIRouter, Depends, HTTPException, Path
from sqlalchemy.orm import Session

from app.database import get_db
from app.dependencies import current_user
from app.exceptions import UserNotFoundError
from app.schemas import profile as schemas
from app.services import profile as services
from app.models.user import User

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



# Routes on the logged-in user's own profile
me_router = APIRouter(prefix="/me/profile", tags=["profile"])


@me_router.patch(
    "",
    response_model=schemas.ProfileOut,
    responses={
        200: {"description": "Profile updated, sent back with the changes"},
        422: {"description": "Display name longer than 30 characters, or unknown avatar color or style"},
    },
)
def update_profile(
    body: schemas.ProfileUpdate,
    user: User = Depends(current_user),
    db: Session = Depends(get_db),
):
    """Change the logged-in user's display name and avatar. A field left out stays the same."""
    return services.update_profile(db, user, body)