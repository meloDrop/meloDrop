"""Routes of the catalog module."""

from fastapi import APIRouter, Depends, HTTPException, Query

from app.dependencies import current_user
from app.exceptions import DeezerError
from app.schemas import catalog as schemas
from app.services import catalog as services

router = APIRouter(
    prefix="/catalog",
    tags=["catalog"],
    dependencies=[Depends(current_user)],
)


@router.get(
    "/search",
    response_model=list[schemas.TrackResult],
    responses={
        200: {"description": "Tracks found on Deezer"},
        502: {"description": "Deezer returned an error or did not answer"},
    },
)
def search(q: str = Query(min_length=1, max_length=100)):
    """Search tracks on Deezer."""
    try:
        return services.search_tracks(q)
    except DeezerError as e:
        raise HTTPException(status_code=502, detail=str(e))