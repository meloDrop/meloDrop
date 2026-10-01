from sqlalchemy import or_
from sqlalchemy.orm import Session

from app.exceptions import InvalidCredentialsError, UserAlreadyExistsError
from app.models.user import User
from app.utils.security import create_access_token, hash_password, verify_password


def register_user(db: Session, email: str, username: str, password: str) -> User:
    if db.query(User).filter(User.email == email).first():
        raise UserAlreadyExistsError("email_taken")

    if db.query(User).filter(User.username == username).first():
        raise UserAlreadyExistsError("username_taken")

    user = User(
        email=email,
        username=username,
        password_hash=hash_password(password),
    )
    db.add(user)
    db.commit()
    db.refresh(user)
    return user


def login_user(db: Session, identifier: str, password: str) -> str:
    user = db.query(User).filter(
        or_(User.email == identifier, User.username == identifier)
    ).first()

    if user is None or not verify_password(password, user.password_hash):
        raise InvalidCredentialsError("invalid_credentials")

    return create_access_token(user.id)