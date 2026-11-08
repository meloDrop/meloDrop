"""Functions that read and change the "Mes likes" list."""

from datetime import timezone

from sqlalchemy.orm import Session

from app.exceptions import LikeNotFoundError
from app.models.liked_song import LikedSong
from app.models.song import Song
from app.schemas.likes import LikedSongOut
from app.models.drop import Drop
from app.models.save import Save
from app.models.user import User
from app.schemas.users import UserBriefOut


def find_droppers(db: Session, user_id: int, song_id: int) -> list[UserBriefOut]:
    """Return the users whose drop of this song the user liked: they got the like.

    Several users when they dropped the same song (ADR-0021), the first drop first.
    An empty list when their accounts were deleted (ADR-0013).
    """
    # liked_songs has no drop: the likes are found in saves, on the drops of this song
    users = (
        db.query(User)
        .join(Drop, Drop.user_id == User.id)
        .join(Save, Save.drop_id == Drop.id)
        .filter(Save.user_id == user_id, Drop.song_id == song_id)
        .order_by(Drop.created_at, Drop.id)
        .all()
    )

    # A user who dropped the song twice (two days) is shown once
    droppers = []
    seen = set()
    for user in users:
        if user.id in seen:
            continue
        seen.add(user.id)
        droppers.append(UserBriefOut.model_validate(user))
    return droppers


def get_likes(db: Session, user_id: int) -> list[LikedSongOut]:
    """Return the user's liked songs, the most recent first."""
    # Each line of liked_songs with its song
    rows = (
        db.query(LikedSong, Song)
        .join(Song, Song.id == LikedSong.song_id)
        .filter(LikedSong.user_id == user_id)
        .order_by(LikedSong.created_at.desc(), LikedSong.id.desc())
        .all()
    )

    # Turn each line into a LikedSongOut
    likes = []
    for liked, song in rows:
        # Rescued from "Mes skips" after the drop expired: no dropper shown,
        # the app writes "Récupéré dans les skips" instead
        if liked.liked_late:
            droppers = []
        else:
            # Empty when the droppers deleted their account: the song stays (ADR-0013)
            droppers = find_droppers(db, user_id, song.id)
        # The first dropper, for the versions of the app before ADR-0021
        author = droppers[0] if droppers else None
        likes.append(
            LikedSongOut(
                song_id=song.id,
                title=song.title,
                artist=song.artist_name,
                cover_url=song.cover_url,
                genre=song.genre,
                liked_at=liked.created_at.replace(tzinfo=timezone.utc),
                author_id=author.id if author else None,
                author_username=author.username if author else None,
                liked_late=liked.liked_late,
                is_pick=liked.is_pick,
                droppers=droppers,
            )
        )
    return likes


def remove_like(db: Session, user_id: int, song_id: int) -> None:
    """Remove a song from the user's "Mes likes" list."""
    # Look for the song in this user's list only
    liked = (
        db.query(LikedSong)
        .filter(LikedSong.user_id == user_id, LikedSong.song_id == song_id)
        .first()
    )

    # Not in the list: nothing to remove
    if liked is None:
        raise LikeNotFoundError("like_not_found")

    # Only the line in liked_songs goes. The line in saves stays,
    # so the author keeps the like received (ADR-0013).
    # A song rescued late goes back to "Mes skips": its skip is still there
    db.delete(liked)
    db.commit()