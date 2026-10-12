"""Functions that save or skip a drop from the Discover feed."""

from sqlalchemy.orm import Session

from app.exceptions import DropAlreadySeenError, DropNotFoundError, OwnDropError
from app.models.drop import Drop
from app.models.liked_song import LikedSong
from app.models.save import Save
from app.models.skip import Skip
from app.utils.time import utc_now


def check_drop(db: Session, user_id: int, drop_id: int) -> Drop:
    """Return the drop if the user can still save or skip it."""
    # The drop must exist and must not be expired
    drop = db.get(Drop, drop_id)
    if drop is None or drop.expires_at <= utc_now():
        raise DropNotFoundError("drop_not_found")

    # A user cannot save or skip their own drop
    if drop.user_id == user_id:
        raise OwnDropError("own_drop")

    # A drop can only be saved or skipped once
    saved = db.query(Save).filter(Save.user_id == user_id, Save.drop_id == drop_id).first()
    skipped = db.query(Skip).filter(Skip.user_id == user_id, Skip.drop_id == drop_id).first()
    if saved is not None or skipped is not None:
        raise DropAlreadySeenError("drop_already_seen")

    return drop


def save_drop(db: Session, user_id: int, drop_id: int) -> None:
    """Like a drop: one row in saves and one row in liked_songs (ADR-0013)."""
    drop = check_drop(db, user_id, drop_id)

    # Remember that the user liked this drop
    db.add(Save(user_id=user_id, drop_id=drop.id))

    # Add the song to "Mes likes", unless it is already there
    liked = (
        db.query(LikedSong)
        .filter(LikedSong.user_id == user_id, LikedSong.song_id == drop.song_id)
        .first()
    )
    if liked is None:
        db.add(LikedSong(user_id=user_id, song_id=drop.song_id))

    # Save both rows together
    db.commit()


def skip_drop(db: Session, user_id: int, drop_id: int) -> None:
    """Skip a drop: one row in skips."""
    check_drop(db, user_id, drop_id)

    # Remember that the user skipped this drop
    db.add(Skip(user_id=user_id, drop_id=drop_id))
    db.commit()