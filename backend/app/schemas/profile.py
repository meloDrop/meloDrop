"""Data received and sent by the profile routes."""

from datetime import datetime
from typing import Literal

from pydantic import BaseModel, Field

from app.schemas.stats import StatsOut

# Background colors an avatar can have (same names in Avatar.kt)
AvatarColor = Literal["lavender", "peach", "butter", "mint", "rose"]

# Drop styles an avatar can have (same names in Avatar.kt)
AvatarStyle = Literal["simple", "rock", "rap", "pop", "classical", "reggae", "electro"]


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
    # The @handle: unique, used to log in, never changes
    username: str
    # Public name chosen by the user, or null: the app then shows the username
    display_name: str | None = None
    # Sign-up date in UTC, for "membre depuis"
    created_at: datetime
    avatar_color: str
    avatar_style: str
    stats: StatsOut
    # The drop online today, or null
    current_drop: ProfileDropOut | None = None
    # The 5 last expired drops, the most recent first
    recent_drops: list[ProfileDropOut]


class ProfileUpdate(BaseModel):
    """What the user can change on their own profile. A field left out stays the same."""

    # New display name; an empty text gives back the username
    display_name: str | None = Field(default=None, max_length=30)
    # One of the 5 background colors
    avatar_color: AvatarColor | None = None
    # One of the 7 drop styles
    avatar_style: AvatarStyle | None = None