"""Data sent back by the feed routes."""

from pydantic import BaseModel

from app.schemas.genres import GenreOut


class FeedItem(BaseModel):
    """A drop shown as a card in the Discover feed."""

    id: int
    song_id: int
    title: str
    artist: str
    cover_url: str
    genres: list[GenreOut]
    author_id: int
    author_username: str
    saves_count: int