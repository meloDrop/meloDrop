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
from app.schemas.users import UserBriefOut
from app.services.auto_badges import SWIPE_RULES, check_auto_badges
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

    # One line per song: a swipe skips every drop of the song (ADR-0021),
    # and the same song can also be skipped again another day
    skips = []
    skips_by_song = {}
    for skip, drop, song, author in rows:
        item = skips_by_song.get(song.id)
        if item is None:
            # The most recent skip of the song gives the date and the drop to rescue
            item = SkippedSongOut(
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
            skips_by_song[song.id] = item
            skips.append(item)
        elif drop.expires_at > now:
            # One drop of the song still online is enough: the rescue gives it a like
            item.drop_active = True

        # Everyone who dropped the skipped song, each one once
        if all(dropper.id != author.id for dropper in item.droppers):
            item.droppers.append(UserBriefOut.model_validate(author))
    return skips


def rescue_skip(db: Session, user_id: int, drop_id: int) -> RescueOut:
    """Move a skipped song to "Mes likes"; each dropper gets a like only if their drop is still active."""
    # The user must have skipped this drop
    skip = db.query(Skip).filter(Skip.user_id == user_id, Skip.drop_id == drop_id).first()
    if skip is None:
        raise SkipNotFoundError("skip_not_found")

    drop = db.get(Drop, drop_id)
    now = utc_now()

    # Every drop of the song the user skipped: the swipe skipped them all (ADR-0021)
    song_skips = (
        db.query(Skip, Drop)
        .join(Drop, Drop.id == Skip.drop_id)
        .filter(Skip.user_id == user_id, Drop.song_id == drop.song_id)
        .all()
    )

    # Drops still active: the skip becomes a real save, their author gets a like.
    # Drops expired: the skip stays, so the drop still counts as seen
    credited = []
    for song_skip, song_drop in song_skips:
        if song_drop.expires_at > now:
            db.delete(song_skip)
            db.add(Save(user_id=user_id, drop_id=song_drop.id))
            credited.append(song_drop.user_id)
    is_active = len(credited) > 0

    # The song goes to "Mes likes". No drop online any more:
    # liked_late shows it as "Récupéré dans les skips"
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

    # A rescue on a drop online is a like: each author can reach an automatic badge
    for author_id in credited:
        check_auto_badges(db, author_id, SWIPE_RULES)

    return RescueOut(song_id=drop.song_id, liked_late=liked.liked_late, credited_count=len(credited))