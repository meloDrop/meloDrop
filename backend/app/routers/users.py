"""Routes of the users module."""

from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.database import get_db
from app.dependencies import current_user
from app.models.user import User
from app.schemas import users as schemas
from app.services import users as services

router = APIRouter(prefix="/users", tags=["users"])


@router.get(
    "/search",
    response_model=list[schemas.UserSearchOut],
    responses={
        200: {"description": "Users whose username starts with q, sorted by username (20 max), or an empty list"},
        422: {"description": "q is missing or shorter than 2 characters"},
    },
)
def search_users(
    q: str = Query(min_length=2, max_length=30, description="Start of the @username, without the @"),
    user: User = Depends(current_user),
    db: Session = Depends(get_db),
):
    """Search users by the start of their username."""
    return services.search_users(db, q, user)