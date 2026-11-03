"""Functions that read the "Mes skips" list and rescue a skipped song."""

from datetime import timezone

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.exceptions import SkipNotFoundError
from app.models.drop import Drop
from app.models.liked_song import LikedSong
from app.models.save import Save
from app.models.skip import Skip
from app.models.song import Song
from app.models.user import User
from app.schemas.skips import RescueOut, SkippedSongOut
from app.utils.time import utc_now

from datetime import timedelta, timezone

# The list is there to catch a mistake: only the skips of the last 7 days are shown
SKIPS_DAYS = 7


def get_skips(db: Session, user_id: int) -> list[SkippedSongOut]:
    """Return the songs the user skipped in the last 7 days, the most recent first."""
    now = utc_now()

    # Songs already in the user's likes: their skips are hidden
    liked = select(LikedSong.song_id).where(LikedSong.user_id == user_id)

    # Each recent skip with its drop, its song and the author of the drop
    rows = (
        db.query(Skip, Drop, Song, User)
        .join(Drop, Drop.id == Skip.drop_id)
        .join(Song, Song.id == Drop.song_id)
        .join(User, User.id == Drop.user_id)
        .filter(
            Skip.user_id == user_id,
            Skip.created_at >= now - timedelta(days=SKIPS_DAYS),
            Drop.song_id.not_in(liked),
        )
        .order_by(Skip.created_at.desc(), Skip.id.desc())
        .all()
    )

    skips = []
    song_ids = set()
    for skip, drop, song, author in rows:
        # Same song skipped on two drops: only the most recent skip is shown
        if song.id in song_ids:
            continue
        song_ids.add(song.id)

        skips.append(
            SkippedSongOut(
                drop_id=drop.id,
                song_id=song.id,
                title=song.title,
                artist=song.artist_name,
                cover_url=song.cover_url,
                skipped_at=skip.created_at.replace(tzinfo=timezone.utc),
                author_id=author.id,
                author_username=author.username,
                drop_active=drop.expires_at > now,
            )
        )
    return skips


def rescue_skip(db: Session, user_id: int, drop_id: int) -> RescueOut:
    """Move a skipped song to "Mes likes"; the author gets a like only if the drop is still active."""
    # The user must have skipped this drop
    skip = db.query(Skip).filter(Skip.user_id == user_id, Skip.drop_id == drop_id).first()
    if skip is None:
        raise SkipNotFoundError("skip_not_found")

    drop = db.get(Drop, drop_id)
    is_active = drop.expires_at > utc_now()

    # Drop still active: the skip becomes a real save, the author gets a like
    if is_active:
        db.delete(skip)
        db.add(Save(user_id=user_id, drop_id=drop.id))

    # Drop expired: the skip stays, so the drop still counts as seen.
    # The song goes to "Mes likes"; liked_late shows it as "Récupéré dans les skips"
    liked = (
        db.query(LikedSong)
        .filter(LikedSong.user_id == user_id, LikedSong.song_id == drop.song_id)
        .first()
    )
    if liked is None:
        liked = LikedSong(user_id=user_id, song_id=drop.song_id, liked_late=not is_active)
        db.add(liked)
    elif is_active:
        # Rescued late before, from an expired drop: this like makes it a normal like
        liked.liked_late = False

    # Save everything together
    db.commit()

    return RescueOut(song_id=drop.song_id, liked_late=liked.liked_late)