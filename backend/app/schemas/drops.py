"""Data received and sent by the drops routes."""

from datetime import datetime

from pydantic import BaseModel

from app.schemas.genres import GenreOut


class DropCreate(BaseModel):
    """Data needed to drop the song of the day."""

    deezer_track_id: int
    genres: list[int] = []


class DropOut(BaseModel):
    """A drop as shown in the app."""

    song_id: int
    title: str
    artist: str
    cover_url: str
    genre: str | None = None
    genres: list[GenreOut]
    expires_at: datetime


class MyDropOut(DropOut):
    """The logged-in user's current drop, with its stats."""

    id: int
    saves_count: int
    genre_reported: bool


class GenresUpdate(BaseModel):
    """New genres chosen by the author of a drop."""

    genres: list[int]