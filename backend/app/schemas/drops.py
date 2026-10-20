"""Data received and sent by the drops routes."""

from datetime import datetime

from pydantic import BaseModel


class DropCreate(BaseModel):
    """Data needed to drop the song of the day."""

    deezer_track_id: int


class DropOut(BaseModel):
    """A drop as shown in the app."""

    song_id: int
    title: str
    artist: str
    cover_url: str
    genre: str | None = None
    expires_at: datetime


class MyDropOut(DropOut):
    """The logged-in user's current drop, with its stats."""

    id: int
    saves_count: int