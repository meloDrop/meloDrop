"""Functions that save or skip a song card from the Discover feed.

A card shows a song once, even when several users dropped it (ADR-0021):
the swipe counts for every drop of the song online now.
"""

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.exceptions import DropAlreadySeenError, DropNotFoundError, OwnDropError
from app.models.drop import Drop
from app.models.liked_song import LikedSong
from app.models.save import Save
from app.models.skip import Skip
from app.services.auto_badges import SWIPE_RULES, check_auto_badges
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


def song_drops(db: Session, user_id: int, drop: Drop) -> list[Drop]:
    """Return the drops of the same song the swipe counts for, the drop itself included.

    Only the drops online now, of the other users, and not swiped yet by the user.
    """
    saved = select(Save.drop_id).where(Save.user_id == user_id)
    skipped = select(Skip.drop_id).where(Skip.user_id == user_id)
    return (
        db.query(Drop)
        .filter(
            Drop.song_id == drop.song_id,
            Drop.expires_at > utc_now(),
            Drop.user_id != user_id,
            Drop.id.not_in(saved),
            Drop.id.not_in(skipped),
        )
        .all()
    )


def save_drop(db: Session, user_id: int, drop_id: int, is_pick: bool = False) -> None:
    """Like the song of a drop: one row in saves per dropper, one row in liked_songs (ADR-0013)."""
    # Check the rules: the drop exists, is not mine, and is not seen yet
    drop = check_drop(db, user_id, drop_id)

    # Every user who dropped the song gets the like, and the pick if it is one
    drops = song_drops(db, user_id, drop)
    for song_drop in drops:
        db.add(Save(user_id=user_id, drop_id=song_drop.id, is_pick=is_pick))

    # Add the song to "Mes likes", unless it is already there
    liked = (
        db.query(LikedSong)
        .filter(LikedSong.user_id == user_id, LikedSong.song_id == drop.song_id)
        .first()
    )
    if liked is None:
        db.add(LikedSong(user_id=user_id, song_id=drop.song_id, is_pick=is_pick))
    else:
        # Rescued late before, from an expired drop: this like makes it a normal like
        liked.liked_late = False
        # A pick marks the song; a simple like never removes an old pick
        if is_pick:
            liked.is_pick = True

    # Save all the rows together
    db.commit()

    # Each dropper can reach an automatic badge: likes, picks, like rate
    for song_drop in drops:
        check_auto_badges(db, song_drop.user_id, SWIPE_RULES)

def pick_drop(db: Session, user_id: int, drop_id: int) -> None:
    """Pick a drop (up swipe): a like that also marks the song as a pick."""
    save_drop(db, user_id, drop_id, is_pick=True)

def skip_drop(db: Session, user_id: int, drop_id: int) -> None:
    """Skip the song of a drop: one row in skips per dropper."""
    # Check the rules: the drop exists, is not mine, and is not seen yet
    drop = check_drop(db, user_id, drop_id)

    # Remember that the user skipped the song, on every drop of it
    drops = song_drops(db, user_id, drop)
    for song_drop in drops:
        db.add(Skip(user_id=user_id, drop_id=song_drop.id))
    db.commit()

    # A skip is a view too: each dropper can reach the 30 views of a like rate badge
    for song_drop in drops:
        check_auto_badges(db, song_drop.user_id, ("like_rate",))