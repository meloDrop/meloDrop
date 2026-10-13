"""Account logic: sign up, log in, change email or password, delete account."""

from sqlalchemy import or_
from sqlalchemy.orm import Session

from app.exceptions import InvalidCredentialsError, UserAlreadyExistsError, InvalidPasswordError
from app.models.user import User
from app.utils.security import create_access_token, hash_password, verify_password

# Regular expressions, used to check the password
import re


def register_user(db: Session, email: str, username: str, password: str) -> User:
    """Create an account if the email and username are free."""
    # The password needs a digit and a special character
    check_password_complexity(password)

    # The email must not be used by another account
    if db.query(User).filter(User.email == email).first():
        raise UserAlreadyExistsError("email_taken")

    # The username must not be used by another account
    if db.query(User).filter(User.username == username).first():
        raise UserAlreadyExistsError("username_taken")

    # Create the user: only the hash of the password is stored
    user = User(
        email=email,
        username=username,
        password_hash=hash_password(password),
    )
    db.add(user)
    db.commit()

    # Reload the user to get its id from the database
    db.refresh(user)
    return user


def login_user(db: Session, identifier: str, password: str) -> str:
    """Check the email or username and the password, then return a token."""
    # Find the user with their email or their username
    user = db.query(User).filter(
        or_(User.email == identifier, User.username == identifier)
    ).first()

    # Same error if the user does not exist or if the password is wrong
    if user is None or not verify_password(password, user.password_hash):
        raise InvalidCredentialsError("invalid_credentials")

    # Create a JWT that contains the user id
    return create_access_token(user.id)


def change_email(db: Session, user: User, password: str, new_email: str) -> User:
    """Change the user's email after checking their password."""
    # The current password must be right
    if not verify_password(password, user.password_hash):
        raise InvalidCredentialsError("invalid_password")

    # The new email must not be used by another account
    if db.query(User).filter(User.email == new_email, User.id != user.id).first():
        raise UserAlreadyExistsError("email_taken")

    # Save the new email
    user.email = new_email
    db.commit()
    db.refresh(user)
    return user


def change_password(db: Session, user: User, old_password: str, new_password: str) -> None:
    """Change the user's password after checking the old one."""
    # The old password must be right
    if not verify_password(old_password, user.password_hash):
        raise InvalidCredentialsError("invalid_password")

    # The new password needs a digit and a special character
    check_password_complexity(new_password)

    # Save the hash of the new password
    user.password_hash = hash_password(new_password)
    db.commit()


def delete_user(db: Session, user: User) -> None:
    """Delete the user and all their data."""
    # MySQL also deletes the user's rows in the other tables (ON DELETE CASCADE)
    db.delete(user)
    db.commit()


def check_password_complexity(password: str) -> None:
    """Raise if the password lacks a digit or a special character."""
    # At least one digit
    if not re.search(r"\d", password):
        raise InvalidPasswordError("password_missing_digit")

    # At least one special character: not a letter, a digit, "_" or a space
    if not re.search(r"[^\w\s]", password):
        raise InvalidPasswordError("password_missing_special_char")