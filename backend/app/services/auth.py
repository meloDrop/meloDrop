from sqlalchemy.orm import Session

from app.exceptions import UserAlreadyExistsError
from app.models.user import User
from app.utils.security import hash_password


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