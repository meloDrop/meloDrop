"""Data sent back by the profile routes."""

from datetime import datetime

from pydantic import BaseModel

from app.schemas.stats import StatsOut


class ProfileDropOut(BaseModel):
    """A drop shown on a profile: the drop of the day or a past one."""

    id: int
    song_id: int
    title: str
    artist: str
    cover_url: str
    genre: str | None = None
    # When the drop was posted, in UTC
    dropped_at: datetime
    # Likes received on this drop
    saves_count: int


class ProfileOut(BaseModel):
    """A user's profile, without private data (no email)."""

    id: int
    username: str
    # Sign-up date in UTC, for "membre depuis"
    created_at: datetime
    avatar_color: str
    avatar_style: str
    stats: StatsOut
    # The drop online today, or null
    current_drop: ProfileDropOut | None = None
    # The 5 last expired drops, the most recent first
    recent_drops: list[ProfileDropOut]