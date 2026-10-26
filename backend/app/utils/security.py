"""Password hashing with Argon2 and login tokens (JWT)."""

from datetime import datetime, timedelta, timezone

import jwt
from argon2 import PasswordHasher
from argon2.exceptions import VerifyMismatchError

from app.config import settings

password_hasher = PasswordHasher()


def hash_password(password: str) -> str:
    """Turn a password into a hash that can be stored in the database."""
    return password_hasher.hash(password)


def verify_password(password: str, password_hash: str) -> bool:
    """Check if a password matches the stored hash."""
    try:
        return password_hasher.verify(password_hash, password)
    except VerifyMismatchError:
        return False


def create_access_token(user_id: int) -> str:
    """Create a signed token containing the user id and an expiration date."""
    expire = datetime.now(timezone.utc) + timedelta(minutes=settings.JWT_EXPIRE_MINUTES)
    payload = {"user_id": user_id, "exp": expire}
    return jwt.encode(payload, settings.JWT_SECRET, algorithm=settings.JWT_ALGORITHM)


def decode_access_token(token: str) -> int:
    """Check a token and return the user id it contains."""
    payload = jwt.decode(token, settings.JWT_SECRET, algorithms=[settings.JWT_ALGORITHM])
    return payload["user_id"]

