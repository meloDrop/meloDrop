"""Data sent back by the songs routes."""

from pydantic import BaseModel


class PreviewOut(BaseModel):
    """A fresh 30-second preview link for a song."""

    preview_url: str | None = None