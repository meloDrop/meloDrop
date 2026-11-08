"""Data sent back by the skips routes."""

from datetime import datetime

from pydantic import BaseModel

from app.schemas.users import UserBriefOut


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
    # First user in droppers, for the versions of the app before ADR-0021
    author_id: int
    author_username: str
    # True while a drop of the song is online: a rescue then gives a like to its author
    drop_active: bool
    # Everyone whose drop of the song was skipped: "drop par 3 personnes" with more than one
    droppers: list[UserBriefOut] = []


class RescueOut(BaseModel):
    """Result of a rescue from "Mes skips"."""

    song_id: int
    # False: the drop was online, the author got a like.
    # True: the drop had expired, no like: "Récupéré dans les skips" in "Mes likes"
    liked_late: bool
    # Number of droppers who got a like: their drop was still online
    credited_count: int = 0