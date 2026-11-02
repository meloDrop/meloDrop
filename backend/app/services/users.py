"""Functions that search for users."""

from sqlalchemy.orm import Session

from app.models.user import User

# Max number of users sent back by one search
SEARCH_SIZE = 20


def search_users(db: Session, query: str, me: User) -> list[User]:
    """Return the users whose username starts with the query, verified accounts only."""
    return (
        db.query(User)
        .filter(
            # Prefix search: "lit" finds "litoxam"; autoescape makes _ and % plain characters
            User.username.startswith(query, autoescape=True),
            # Accounts not verified cannot log in, so they are hidden
            User.email_verified.is_(True),
            # The user does not find themselves
            User.id != me.id,
        )
        .order_by(User.username)
        .limit(SEARCH_SIZE)
        .all()
    )