"""Routes of the songs module."""

from fastapi import APIRouter, Depends, HTTPException, Path
from sqlalchemy.orm import Session

from app.database import get_db
from app.dependencies import current_user
from app.exceptions import DeezerError, SongNotFoundError, TrackNotFoundError
from app.schemas import songs as schemas
from app.services import songs as services

router = APIRouter(
    prefix="/songs",
    tags=["songs"],
    dependencies=[Depends(current_user)],
)


@router.get(
    "/{song_id}/preview",
    response_model=schemas.PreviewOut,
    responses={
        200: {"description": "Fresh preview link"},
        404: {"description": "Song not found in meloDrop or on Deezer"},
        502: {"description": "Deezer returned an error or did not answer"},
    },
)
def get_preview(
    song_id: int = Path(description="meloDrop song id (song_id in DropOut), not the Deezer id"),
    db: Session = Depends(get_db),
):
    """Get a fresh 30-second preview link for a song."""
    try:
        return services.get_preview(db, song_id)
    except SongNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except TrackNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except DeezerError as e:
        raise HTTPException(status_code=502, detail=str(e))


@router.get(
    "/{song_id}",
    response_model=schemas.SongDetailOut,
    responses={
        200: {"description": "The song, its Deezer details (null when Deezer does not answer) and its meloDrop counters"},
        404: {"description": "Song not found in meloDrop"},
    },
)
def get_song(
    song_id: int = Path(description="meloDrop song id (song_id in the lists), not the Deezer id"),
    db: Session = Depends(get_db),
):
    """Everything about a song, for its poster in the app."""
    try:
        return services.get_song(db, song_id)
    except SongNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
