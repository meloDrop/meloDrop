"""Routes of the podium module."""

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.database import get_db
from app.dependencies import current_user
from app.exceptions import DeezerError, DuplicateSongError, TrackNotFoundError
from app.models.user import User
from app.schemas.podium import PodiumSongOut, PodiumUpdate
from app.services import podium as services

router = APIRouter(prefix="/me/podium", tags=["podium"])


@router.put(
    "",
    response_model=list[PodiumSongOut],
    responses={
        200: {"description": "The new podium, from the 1st place to the last"},
        400: {"description": "The same song is given twice"},
        404: {"description": "Track not found on Deezer"},
        422: {"description": "More than 3 songs"},
        502: {"description": "Deezer returned an error or did not answer"},
    },
)
def set_podium(body: PodiumUpdate, user: User = Depends(current_user), db: Session = Depends(get_db)):
    """Replace the logged-in user's podium. The first song goes to the 1st place."""
    try:
        return services.set_podium(db, user.id, body.deezer_track_ids)
    except DuplicateSongError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except TrackNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except DeezerError as e:
        raise HTTPException(status_code=502, detail=str(e))