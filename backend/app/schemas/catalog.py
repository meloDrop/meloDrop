"""Data sent back by the catalog routes."""

from pydantic import BaseModel


class TrackResult(BaseModel):
    """A track found on Deezer, as shown in the search results."""

    deezer_track_id: int
    title: str
    artist: str
    cover_url: str | None = None
    preview_url: str | None = None