"""Routes of the likes module."""

from fastapi import APIRouter, Depends, HTTPException, Path
from sqlalchemy.orm import Session

from app.database import get_db
from app.dependencies import current_user
from app.exceptions import LikeNotFoundError
from app.models.user import User
from app.schemas import likes as schemas
from app.services import likes as services

router = APIRouter(prefix="/me/likes", tags=["likes"])


@router.get(
    "",
    response_model=list[schemas.LikedSongOut],
    responses={200: {"description": "The liked songs, the most recent first, or an empty list"}},
)
def get_likes(user: User = Depends(current_user), db: Session = Depends(get_db)):
    """Get the logged-in user's "Mes likes" list."""
    return services.get_likes(db, user.id)


@router.delete(
    "/{song_id}",
    status_code=204,
    responses={
        204: {"description": "Song removed from the list"},
        404: {"description": "The song is not in the user's list"},
    },
)
def remove_like(
    song_id: int = Path(description="meloDrop song id (song_id in LikedSongOut), not the Deezer id"),
    user: User = Depends(current_user),
    db: Session = Depends(get_db),
):
    """Remove a song from the logged-in user's "Mes likes" list."""
    try:
        services.remove_like(db, user.id, song_id)
    except LikeNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))