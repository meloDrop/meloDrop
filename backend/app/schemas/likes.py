"""Data sent back by the likes routes."""

from datetime import datetime

from pydantic import BaseModel


class LikedSongOut(BaseModel):
    """A song in the user's "Mes likes" list."""

    song_id: int
    title: str
    artist: str
    cover_url: str | None = None
    # When the user liked it, in UTC
    liked_at: datetime
    # Author of the drop the user liked: null if the account was deleted (ADR-0013)
    author_id: int | None = None
    author_username: str | None = None
    # True when rescued from "Mes skips" after the drop expired: "Récupéré dans les skips"
    liked_late: bool = False