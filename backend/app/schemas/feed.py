"""Data sent back by the feed routes."""

from pydantic import BaseModel


class FeedItem(BaseModel):
    """A drop shown as a card in the Discover feed."""

    id: int
    song_id: int
    title: str
    artist: str
    cover_url: str | None = None
    genre: str | None = None
    author_id: int
    author_username: str
    saves_count: int
    # True when the logged-in user follows the author: "Abonné" on the card
    is_followed: bool = False