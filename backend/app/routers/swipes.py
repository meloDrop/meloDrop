"""Routes of the swipes module."""

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.database import get_db
from app.dependencies import current_user
from app.exceptions import DropAlreadySeenError, DropNotFoundError, OwnDropError
from app.models.user import User
from app.services import swipes as services

router = APIRouter(prefix="/drops", tags=["swipes"])

ERRORS = {
    204: {"description": "Done, no content"},
    403: {"description": "This is the user's own drop"},
    404: {"description": "Drop not found or expired"},
    409: {"description": "Drop already saved or skipped"},
}


@router.post("/{drop_id}/save", status_code=204, responses=ERRORS)
def save_drop(drop_id: int, user: User = Depends(current_user), db: Session = Depends(get_db)):
    """Like a drop (right swipe) and add its song to "Mes likes"."""
    try:
        services.save_drop(db, user.id, drop_id)
    except DropNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except OwnDropError as e:
        raise HTTPException(status_code=403, detail=str(e))
    except DropAlreadySeenError as e:
        raise HTTPException(status_code=409, detail=str(e))


@router.post("/{drop_id}/skip", status_code=204, responses=ERRORS)
def skip_drop(drop_id: int, user: User = Depends(current_user), db: Session = Depends(get_db)):
    """Skip a drop (left swipe)."""
    try:
        services.skip_drop(db, user.id, drop_id)
    except DropNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except OwnDropError as e:
        raise HTTPException(status_code=403, detail=str(e))
    except DropAlreadySeenError as e:
        raise HTTPException(status_code=409, detail=str(e))