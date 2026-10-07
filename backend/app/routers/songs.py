"""Routes of the songs module."""

from fastapi import APIRouter, Depends, HTTPException
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
def get_preview(song_id: int, db: Session = Depends(get_db)):
    """Get a fresh 30-second preview link for a song."""
    try:
        return services.get_preview(db, song_id)
    except SongNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except TrackNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except DeezerError as e:
        raise HTTPException(status_code=502, detail=str(e))