"""Routes for linking streaming accounts."""

import jwt
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.database import get_db
from app.dependencies import current_user
from app.exceptions import OAuthDeniedError
from app.models.user import User
from app.schemas.links import DeezerStartOut
from app.services import links as services

router = APIRouter(prefix="/links", tags=["links"])


@router.get("/deezer/start", response_model=DeezerStartOut)
def deezer_start(user: User = Depends(current_user)):
    """Return the Deezer authorization URL for the logged-in user."""
    return DeezerStartOut(auth_url=services.start_deezer_link(user.id))


@router.get("/deezer/callback")
def deezer_callback(
    state: str,
    code: str | None = None,
    error_reason: str | None = None,
    db: Session = Depends(get_db),
):
    """Receive Deezer's OAuth redirect, exchange the code, and store the link."""
    try:
        services.handle_deezer_callback(db, code, state, error_reason)
    except OAuthDeniedError:
        raise HTTPException(status_code=400, detail="oauth_denied")
    except jwt.InvalidTokenError:
        raise HTTPException(status_code=400, detail="invalid_state")
    return {"provider": "deezer", "linked": True}
