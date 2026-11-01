"""Actions of the admin on the accounts."""

from sqlalchemy.orm import Session

from app.exceptions import (
    DropNotFoundError,
    OwnAccountError,
    UserAlreadyExistsError,
    UserNotFoundError,
    UsernameMismatchError,
)
from app.models.user import User
from app.schemas.profile import ProfileOut, ProfileUpdate
from app.services.auth import delete_user
from app.services.drops import get_active_drop
from app.services.profile import update_profile


def list_users(db: Session) -> list[User]:
    """Return every account, newest first."""
    return db.query(User).order_by(User.created_at.desc()).all()


def find_user(db: Session, user_id: int) -> User:
    """Return the user with this id, or raise."""
    user = db.get(User, user_id)
    if user is None:
        raise UserNotFoundError("user_not_found")
    return user


def set_email(db: Session, user_id: int, new_email: str) -> User:
    """Replace the email at once, without code, and mark it verified."""
    user = find_user(db, user_id)

    # The new email must be different from the current one
    if new_email.lower() == user.email.lower():
        raise UserAlreadyExistsError("same_email")

    # The new email must not be used by another account
    if db.query(User).filter(User.email == new_email, User.id != user.id).first():
        raise UserAlreadyExistsError("email_taken")

    # The admin vouches for the new email: no code to type
    user.email = new_email
    user.email_verified = True
    # An email change waiting for its code is cancelled
    user.pending_email = None
    db.commit()
    db.refresh(user)
    return user


def update_user_profile(db: Session, user_id: int, changes: ProfileUpdate) -> ProfileOut:
    """Change the display name or the avatar of a user, with the rules of PATCH /me/profile."""
    user = find_user(db, user_id)
    return update_profile(db, user, changes)


def verify_email(db: Session, user_id: int) -> User:
    """Mark the email as verified, without code."""
    user = find_user(db, user_id)
    user.email_verified = True
    db.commit()
    db.refresh(user)
    return user


def delete_active_drop(db: Session, user_id: int) -> None:
    """Delete the user's drop of the day."""
    user = find_user(db, user_id)

    # Same function as GET /drops/me: the drop that has not expired
    drop = get_active_drop(db, user.id)
    if drop is None:
        raise DropNotFoundError("drop_not_found")

    # MySQL also deletes its saves and skips (ON DELETE CASCADE)
    db.delete(drop)
    db.commit()


def delete_account(db: Session, admin: User, user_id: int, username: str) -> None:
    """Delete an account, after checking the typed username."""
    user = find_user(db, user_id)

    # The admin deletes their own account with DELETE /me, not from here
    if user.id == admin.id:
        raise OwnAccountError("own_account")

    # The typed username must match: a wrong id can't delete the wrong account
    if username != user.username:
        raise UsernameMismatchError("username_mismatch")

    delete_user(db, user)