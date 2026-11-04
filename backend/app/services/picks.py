"""Functions that read and change the "Mes picks" list."""

from sqlalchemy.orm import Session

from app.exceptions import LikeNotFoundError
from app.models.liked_song import LikedSong
from app.schemas.likes import LikedSongOut
from app.services.likes import get_likes


def get_picks(db: Session, user_id: int) -> list[LikedSongOut]:
    """Return the user's picks, the most recent like first."""
    # A pick is a like with a mark: same list, same order, same author
    picks = []
    for like in get_likes(db, user_id):
        if like.is_pick:
            picks.append(like)
    return picks


def find_like(db: Session, user_id: int, song_id: int) -> LikedSong:
    """Return the song's line in the user's "Mes likes" list."""
    liked = (
        db.query(LikedSong)
        .filter(LikedSong.user_id == user_id, LikedSong.song_id == song_id)
        .first()
    )
    # Only a song of "Mes likes" can become a pick or stop being one
    if liked is None:
        raise LikeNotFoundError("like_not_found")
    return liked


def add_pick(db: Session, user_id: int, song_id: int) -> None:
    """Turn a song of "Mes likes" into a pick."""
    liked = find_like(db, user_id, song_id)
    # Only the mark changes: the author already got the like, nothing is added
    liked.is_pick = True
    db.commit()


def remove_pick(db: Session, user_id: int, song_id: int) -> None:
    """Turn a pick back into a simple like: the song stays in "Mes likes"."""
    liked = find_like(db, user_id, song_id)
    liked.is_pick = False
    db.commit()