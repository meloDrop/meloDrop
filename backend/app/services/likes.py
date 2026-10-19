"""Functions that read and change the "Mes likes" list."""

from datetime import timezone

from sqlalchemy.orm import Session

from app.exceptions import LikeNotFoundError
from app.models.liked_song import LikedSong
from app.models.song import Song
from app.schemas.likes import LikedSongOut


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
        likes.append(
            LikedSongOut(
                song_id=song.id,
                title=song.title,
                artist=song.artist_name,
                cover_url=song.cover_url,
                liked_at=liked.created_at.replace(tzinfo=timezone.utc),
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
    # so the author keeps the like received (ADR-0013)
    db.delete(liked)
    db.commit()