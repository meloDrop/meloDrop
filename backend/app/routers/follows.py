"""Routes of the follows module."""

from fastapi import APIRouter, Depends, HTTPException, Path
from sqlalchemy.orm import Session

from app.database import get_db
from app.dependencies import current_user
from app.exceptions import CannotFollowSelfError, UserNotFoundError
from app.models.user import User
from app.schemas.follows import FollowUserOut
from app.services import follows as services

router = APIRouter(prefix="/users", tags=["follows"])


@router.post(
    "/{user_id}/follow",
    status_code=204,
    responses={
        204: {"description": "Done, no content. Already followed: nothing changes"},
        400: {"description": "A user can't follow their own account"},
        404: {"description": "No user has this id"},
    },
)
def follow_user(
    user_id: int = Path(description="meloDrop id of the user to follow"),
    user: User = Depends(current_user),
    db: Session = Depends(get_db),
):
    """Follow a user: their drops can come first in the logged-in user's Discover."""
    try:
        services.follow_user(db, user.id, user_id)
    except UserNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except CannotFollowSelfError as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.delete(
    "/{user_id}/follow",
    status_code=204,
    responses={
        204: {"description": "Done, no content. Not followed: nothing changes"},
        404: {"description": "No user has this id"},
    },
)
def unfollow_user(
    user_id: int = Path(description="meloDrop id of the user to unfollow"),
    user: User = Depends(current_user),
    db: Session = Depends(get_db),
):
    """Stop following a user."""
    try:
        services.unfollow_user(db, user.id, user_id)
    except UserNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))


@router.get(
    "/{user_id}/followers",
    response_model=list[FollowUserOut],
    responses={
        200: {"description": "The users who follow this user, the most recent first, or an empty list"},
        404: {"description": "No user has this id"},
    },
    dependencies=[Depends(current_user)],
)
def get_followers(
    user_id: int = Path(description="meloDrop user id"),
    db: Session = Depends(get_db),
):
    """List the followers of a user. Visible by every logged-in user."""
    try:
        return services.get_followers(db, user_id)
    except UserNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))


@router.get(
    "/{user_id}/following",
    response_model=list[FollowUserOut],
    responses={
        200: {"description": "The users this user follows, the most recent first, or an empty list"},
        404: {"description": "No user has this id"},
    },
    dependencies=[Depends(current_user)],
)
def get_following(
    user_id: int = Path(description="meloDrop user id"),
    db: Session = Depends(get_db),
):
    """List the users a user follows. Visible by every logged-in user."""
    try:
        return services.get_following(db, user_id)
    except UserNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
