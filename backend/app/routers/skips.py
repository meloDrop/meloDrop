"""Routes of the skips module."""

from fastapi import APIRouter, Depends, HTTPException, Path
from sqlalchemy.orm import Session

from app.database import get_db
from app.dependencies import current_user
from app.exceptions import SkipNotFoundError
from app.models.user import User
from app.schemas import skips as schemas
from app.services import skips as services

router = APIRouter(prefix="/me/skips", tags=["skips"])


@router.get(
    "",
    response_model=list[schemas.SkippedSongOut],
    responses={200: {"description": "The songs skipped in the last 7 days and not liked since, the most recent first, or an empty list"}},
)
def get_skips(user: User = Depends(current_user), db: Session = Depends(get_db)):
    """Get the logged-in user's "Mes skips" list."""
    return services.get_skips(db, user.id)


@router.post(
    "/{drop_id}/rescue",
    response_model=schemas.RescueOut,
    responses={
        200: {"description": "Song added to \"Mes likes\": the author gets a like only if the drop is still active"},
        404: {"description": "The user has not skipped this drop"},
    },
)
def rescue_skip(
    drop_id: int = Path(description="Id of the skipped drop (drop_id in SkippedSongOut)"),
    user: User = Depends(current_user),
    db: Session = Depends(get_db),
):
    """Rescue a skipped song and add it to "Mes likes"."""
    try:
        return services.rescue_skip(db, user.id, drop_id)
    except SkipNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))