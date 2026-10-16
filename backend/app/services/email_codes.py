"""Email codes logic: create, send and check the 6-digit codes."""

import secrets
from datetime import datetime, timedelta, timezone

from sqlalchemy.orm import Session

from app.config import settings
from app.exceptions import InvalidCodeError
from app.models.email_code import EmailCode
from app.models.user import User
from app.utils.mail import send_code_email
from app.utils.security import hash_password, verify_password


def now_utc() -> datetime:
    """Current UTC time without time zone, like the DATETIME columns of MySQL."""
    return datetime.now(timezone.utc).replace(tzinfo=None)


def create_and_send_code(db: Session, user: User, purpose: str) -> None:
    """Create a new 6-digit code for the user, save its hash, then email it."""
    # Random code from 000000 to 999999, with a generator made for secrets
    code = f"{secrets.randbelow(1_000_000):06d}"

    # One code per user and purpose: the new one replaces the old one
    db.query(EmailCode).filter(
        EmailCode.user_id == user.id, EmailCode.purpose == purpose
    ).delete()

    # Only the hash of the code is stored, like for passwords
    db.add(EmailCode(
        user_id=user.id,
        purpose=purpose,
        code_hash=hash_password(code),
        expires_at=now_utc() + timedelta(minutes=settings.EMAIL_CODE_EXPIRE_MINUTES),
    ))
    db.commit()

    # Send the code after saving it: if the email fails, the user can ask again
    send_code_email(user.email, purpose, code, settings.EMAIL_CODE_EXPIRE_MINUTES)


def check_code(db: Session, user: User, purpose: str, code: str) -> None:
    """Check the code typed by the user and delete it if right, raise otherwise."""
    email_code = db.query(EmailCode).filter(
        EmailCode.user_id == user.id, EmailCode.purpose == purpose
    ).first()

    # No code for this user, or the code is too old
    if email_code is None or email_code.expires_at < now_utc():
        raise InvalidCodeError("code_expired")

    # Too many wrong codes: the user must ask for a new one
    if email_code.attempts >= settings.EMAIL_CODE_MAX_ATTEMPTS:
        raise InvalidCodeError("too_many_attempts")

    # Wrong code: count the try
    if not verify_password(code, email_code.code_hash):
        email_code.attempts += 1
        db.commit()
        raise InvalidCodeError("invalid_code")

    # Right code: it can be used only once
    db.delete(email_code)
    db.commit()