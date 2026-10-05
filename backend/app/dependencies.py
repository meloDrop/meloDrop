"""Shared dependencies for the routes."""

import jwt
from fastapi import Depends, HTTPException
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.user import User
from app.utils.security import decode_access_token

security = HTTPBearer(auto_error=False)


def current_user(
    token_header: HTTPAuthorizationCredentials | None = Depends(security),
    db: Session = Depends(get_db),
) -> User:
    """Return the logged-in user from the token, or answer 401."""
    if token_header is None:
        raise HTTPException(status_code=401, detail="not_authenticated")

    try:
        user_id = decode_access_token(token_header.credentials)
    except jwt.InvalidTokenError:
        raise HTTPException(status_code=401, detail="invalid_token")

    user = db.query(User).filter(User.id == user_id).first()
    if user is None:
        raise HTTPException(status_code=401, detail="invalid_token")

    return user
