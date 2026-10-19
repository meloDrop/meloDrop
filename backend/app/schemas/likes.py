"""Data sent back by the likes routes."""

from datetime import datetime

from pydantic import BaseModel


class LikedSongOut(BaseModel):
    """A song in the user's "Mes likes" list."""

    song_id: int
    title: str
    artist: str
    cover_url: str
    # When the user liked it, in UTC
    liked_at: datetime