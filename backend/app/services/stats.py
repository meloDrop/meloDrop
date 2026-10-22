"""Functions that count the numbers shown on a profile."""

from sqlalchemy.orm import Session

from app.models.drop import Drop
from app.models.liked_song import LikedSong
from app.models.save import Save
from app.schemas.stats import StatsOut


def get_stats(db: Session, user_id: int) -> StatsOut:
    """Return the three counters of a user's profile."""
    # Drops shared by the user, expired ones included
    drops_count = db.query(Drop).filter(Drop.user_id == user_id).count()

    # Likes received: the saves made on the user's drops
    likes_received = (
        db.query(Save)
        .join(Drop, Drop.id == Save.drop_id)
        .filter(Drop.user_id == user_id)
        .count()
    )

    # Songs in the user's "Mes likes" list (ADR-0013)
    likes_count = db.query(LikedSong).filter(LikedSong.user_id == user_id).count()

    return StatsOut(
        drops_count=drops_count,
        likes_received=likes_received,
        likes_count=likes_count,
    )