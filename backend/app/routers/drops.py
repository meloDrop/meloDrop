"""Routes of the drops module."""

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.database import get_db
from app.dependencies import current_user
from app.exceptions import DeezerError, DropAlreadyActiveError, DropNotFoundError, NotDropOwnerError, TrackNotFoundError
from app.models.user import User
from app.schemas import drops as schemas
from app.services import drops as services

router = APIRouter(prefix="/drops", tags=["drops"])


@router.post(
    "",
    status_code=201,
    response_model=schemas.DropOut,
    responses={
        201: {"description": "Drop created"},
        404: {"description": "Track not found on Deezer"},
        409: {"description": "The user already has a drop that has not expired"},
        502: {"description": "Deezer returned an error or did not answer"},
    },
)
def create_drop(body: schemas.DropCreate, user: User = Depends(current_user), db: Session = Depends(get_db)):
    """Drop the song of the day."""
    try:
        return services.create_drop(db, user.id, body.deezer_track_id, body.genres)
    except DropAlreadyActiveError as e:
        raise HTTPException(status_code=409, detail=str(e))
    except TrackNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except DeezerError as e:
        raise HTTPException(status_code=502, detail=str(e))


@router.get(
    "/me",
    response_model=schemas.MyDropOut | None,
    responses={200: {"description": "The current drop, or null if there is none"}},
)
def get_my_drop(user: User = Depends(current_user), db: Session = Depends(get_db)):
    """Get the logged-in user's current drop, or null if there is none."""
    return services.get_my_drop(db, user.id)


@router.patch(
    "/{drop_id}/genres",
    response_model=schemas.MyDropOut,
    responses={
        200: {"description": "Genres updated, genre reports cleared"},
        403: {"description": "The drop belongs to another user"},
        404: {"description": "Drop not found or expired"},
    },
)
def update_genres(drop_id: int, body: schemas.GenresUpdate, user: User = Depends(current_user), db: Session = Depends(get_db)):
    """Replace the genres of the logged-in user's current drop."""
    try:
        return services.update_genres(db, user.id, drop_id, body.genres)
    except DropNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except NotDropOwnerError as e:
        raise HTTPException(status_code=403, detail=str(e))