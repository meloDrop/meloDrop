"""Functions that follow and unfollow users, and read who follows who."""

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.exceptions import CannotFollowSelfError, UserNotFoundError
from app.models.follow import Follow
from app.models.user import User


def find_user(db: Session, user_id: int) -> User:
    """Return the user with this id, or raise."""
    user = db.get(User, user_id)
    if user is None:
        raise UserNotFoundError("user_not_found")
    return user


def follow_user(db: Session, follower_id: int, followed_id: int) -> None:
    """Follow a user. Following again someone already followed changes nothing."""
    # The user to follow must exist
    find_user(db, followed_id)

    if follower_id == followed_id:
        raise CannotFollowSelfError("cannot_follow_self")

    if is_following(db, follower_id, followed_id):
        return

    db.add(Follow(follower_id=follower_id, followed_id=followed_id))
    db.commit()


def unfollow_user(db: Session, follower_id: int, followed_id: int) -> None:
    """Stop following a user. Unfollowing someone not followed changes nothing."""
    find_user(db, followed_id)

    db.query(Follow).filter(
        Follow.follower_id == follower_id,
        Follow.followed_id == followed_id,
    ).delete()
    db.commit()


def is_following(db: Session, follower_id: int, followed_id: int) -> bool:
    """True when the first user follows the second one."""
    row = (
        db.query(Follow)
        .filter(Follow.follower_id == follower_id, Follow.followed_id == followed_id)
        .first()
    )
    return row is not None


def followed_ids(db: Session, user_id: int) -> set[int]:
    """Ids of the users followed by this user."""
    return set(db.scalars(select(Follow.followed_id).where(Follow.follower_id == user_id)))


def count_followers(db: Session, user_id: int) -> int:
    """Number of users who follow this user."""
    return db.query(Follow).filter(Follow.followed_id == user_id).count()


def count_following(db: Session, user_id: int) -> int:
    """Number of users this user follows."""
    return db.query(Follow).filter(Follow.follower_id == user_id).count()


def get_followers(db: Session, user_id: int) -> list[User]:
    """Return the users who follow this user, the most recent follow first."""
    find_user(db, user_id)
    return (
        db.query(User)
        .join(Follow, Follow.follower_id == User.id)
        .filter(Follow.followed_id == user_id)
        .order_by(Follow.created_at.desc(), Follow.id.desc())
        .all()
    )


def get_following(db: Session, user_id: int) -> list[User]:
    """Return the users this user follows, the most recent follow first."""
    find_user(db, user_id)
    return (
        db.query(User)
        .join(Follow, Follow.followed_id == User.id)
        .filter(Follow.follower_id == user_id)
        .order_by(Follow.created_at.desc(), Follow.id.desc())
        .all()
    )
