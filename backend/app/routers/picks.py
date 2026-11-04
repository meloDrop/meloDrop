"""Routes of the picks module."""

from fastapi import APIRouter, Depends, HTTPException, Path
from sqlalchemy.orm import Session

from app.database import get_db
from app.dependencies import current_user
from app.exceptions import LikeNotFoundError
from app.models.user import User
from app.schemas.likes import LikedSongOut
from app.services import picks as services

router = APIRouter(prefix="/me/picks", tags=["picks"])

# Same answers for the two routes that change a pick
CHANGE_RESPONSES = {
    204: {"description": "Done, no content"},
    404: {"description": "The song is not in the user's \"Mes likes\" list"},
}


@router.get(
    "",
    response_model=list[LikedSongOut],
    responses={200: {"description": "The picks, the most recent like first, or an empty list"}},
)
def get_picks(user: User = Depends(current_user), db: Session = Depends(get_db)):
    """Get the logged-in user's "Mes picks" list."""
    return services.get_picks(db, user.id)


@router.post("/{song_id}", status_code=204, responses=CHANGE_RESPONSES)
def add_pick(
    song_id: int = Path(description="meloDrop song id (song_id in LikedSongOut), not the Deezer id"),
    user: User = Depends(current_user),
    db: Session = Depends(get_db),
):
    """Turn a song of "Mes likes" into a pick."""
    try:
        services.add_pick(db, user.id, song_id)
    except LikeNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))


@router.delete("/{song_id}", status_code=204, responses=CHANGE_RESPONSES)
def remove_pick(
    song_id: int = Path(description="meloDrop song id (song_id in LikedSongOut), not the Deezer id"),
    user: User = Depends(current_user),
    db: Session = Depends(get_db),
):
    """Turn a pick back into a simple like: the song stays in "Mes likes"."""
    try:
        services.remove_pick(db, user.id, song_id)
    except LikeNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))