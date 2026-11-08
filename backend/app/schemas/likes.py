"""Data sent back by the likes routes."""

from datetime import datetime

from pydantic import BaseModel

from app.schemas.users import UserBriefOut


class LikedSongOut(BaseModel):
    """A song in the user's "Mes likes" list."""

    song_id: int
    title: str
    artist: str
    cover_url: str | None = None
    # Genre of the album on Deezer, or null: the lists can be sorted by genre
    genre: str | None = None
    # When the user liked it, in UTC
    liked_at: datetime
    # First user in droppers: null if their accounts were deleted (ADR-0013)
    author_id: int | None = None
    author_username: str | None = None
    # True when rescued from "Mes skips" after the drop expired: "Récupéré dans les skips"
    liked_late: bool = False
    # True when the song is a pick: the star is full in "Mes likes"
    is_pick: bool = False
    # Users whose drop of the song the user liked: they all got the like (ADR-0021).
    # "drop par @x" with one user, "drop par 3 personnes" with more
    droppers: list[UserBriefOut] = []