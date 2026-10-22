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



def find_author(db: Session, user_id: int, song_id: int) -> User | None:
    """Return the author of the drop the user liked for this song, or None."""
    # liked_songs has no drop: the like is found in saves, on a drop of this song
    return (
        db.query(User)
        .join(Drop, Drop.user_id == User.id)
        .join(Save, Save.drop_id == Drop.id)
        .filter(Save.user_id == user_id, Drop.song_id == song_id)
        .order_by(Save.created_at.desc())
        .first()
    )


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
        # None when the author deleted their account: the song stays (ADR-0013)
        author = find_author(db, user_id, song.id)
        likes.append(
            LikedSongOut(
                song_id=song.id,
                title=song.title,
                artist=song.artist_name,
                cover_url=song.cover_url,
                liked_at=liked.created_at.replace(tzinfo=timezone.utc),
                author_id=author.id if author else None,
                author_username=author.username if author else None,
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