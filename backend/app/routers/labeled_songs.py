"""Routes of the labeled songs module."""

from fastapi import APIRouter, Depends, HTTPException, Path
from sqlalchemy.orm import Session

from app.database import get_db
from app.dependencies import current_user
from app.exceptions import DeezerError, LabeledSongNotFoundError, TrackNotFoundError
from app.models.user import User
from app.schemas.labeled_songs import LabeledSongOut, LabeledSongUpdate, SongLabel
from app.services import labeled_songs as services

router = APIRouter(prefix="/me/labels", tags=["labeled songs"])


@router.put(
    "/{label}",
    response_model=list[LabeledSongOut],
    responses={
        200: {"description": "All the labeled songs of the user, in the order of the labels"},
        404: {"description": "Track not found on Deezer"},
        422: {"description": "Unknown label"},
        502: {"description": "Deezer returned an error or did not answer"},
    },
)
def set_labeled_song(
    body: LabeledSongUpdate,
    label: SongLabel = Path(description="Code of the label, like cry or party"),
    user: User = Depends(current_user),
    db: Session = Depends(get_db),
):
    """Put a song under a label of the logged-in user's Mixtape, or replace the one already there."""
    try:
        return services.set_labeled_song(db, user.id, label, body.deezer_track_id)
    except TrackNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except DeezerError as e:
        raise HTTPException(status_code=502, detail=str(e))


@router.delete(
    "/{label}",
    status_code=204,
    responses={
        204: {"description": "Done, no content"},
        404: {"description": "No song under this label"},
        422: {"description": "Unknown label"},
    },
)
def remove_labeled_song(
    label: SongLabel = Path(description="Code of the label, like cry or party"),
    user: User = Depends(current_user),
    db: Session = Depends(get_db),
):
    """Remove the song of a label from the logged-in user's Mixtape."""
    try:
        services.remove_labeled_song(db, user.id, label)
    except LabeledSongNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
