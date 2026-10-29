"""Data sent back by the skips routes."""

from datetime import datetime

from pydantic import BaseModel


class SkippedSongOut(BaseModel):
    """A song in the user's "Mes skips" list."""

    # Id of the skipped drop: used to rescue it
    drop_id: int
    song_id: int
    title: str
    artist: str
    cover_url: str | None = None
    # When the user skipped it, in UTC
    skipped_at: datetime
    # Author of the skipped drop
    author_id: int
    author_username: str
    # True while the drop is online: a rescue then gives a like to the author
    drop_active: bool


class RescueOut(BaseModel):
    """Result of a rescue from "Mes skips"."""

    song_id: int
    # False: the drop was online, the author got a like.
    # True: the drop had expired, no like: "Récupéré dans les skips" in "Mes likes"
    liked_late: bool