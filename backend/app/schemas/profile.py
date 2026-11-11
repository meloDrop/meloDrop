"""Data received and sent by the profile routes."""

from datetime import datetime
from typing import Literal

from pydantic import BaseModel, Field

from app.schemas.stats import StatsOut
from app.schemas.badges import UserBadgeOut
from app.schemas.labeled_songs import LabeledSongOut
from app.schemas.podium import PodiumSongOut

# Background colors an avatar can have (same names in Avatar.kt)
AvatarColor = Literal["lavender", "peach", "butter", "mint", "rose"]

# Drop styles an avatar can have (same names in Avatar.kt)
AvatarStyle = Literal["simple", "rock", "rap", "pop", "classical", "reggae", "electro", "jazz", "lofi"]

# Colors of the drop. The last 4 are premium (PREMIUM_DROPS in services/profile.py)
AvatarDrop = Literal[
    "night", "cream", "lavender", "peach", "butter", "mint", "rose",
    "ocean", "plum", "gold", "spectre",
]

# What is drawn on the background, behind the drop
AvatarDeco = Literal["plain", "gradient", "waves", "notes"]

# Face drawn on the drop
AvatarFace = Literal["none", "smile", "wink", "shades", "closed", "stars"]

# Animated aura around the avatar, on the profiles. Every aura but "none" is premium
AvatarAura = Literal[
    "none", "halo", "bolts", "chain", "sparkles", "staff", "rasta", "equalizer", "smoke", "rain",
]


class ProfileDropOut(BaseModel):
    """A drop shown on a profile: the drop of the day or a past one."""

    id: int
    song_id: int
    title: str
    artist: str
    cover_url: str | None = None
    genre: str | None = None
    # When the drop was posted, in UTC
    dropped_at: datetime
    # Likes received on this drop, picks included
    saves_count: int
    # Picks received on this drop (part of saves_count)
    picks_count: int
    # Users who swiped this drop: likes, picks and skips
    views_count: int


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
    avatar_drop: str
    avatar_deco: str
    avatar_face: str
    avatar_aura: str
    stats: StatsOut
    # The drop online today, or null
    current_drop: ProfileDropOut | None = None
    # The 5 last expired drops, the most recent first
    recent_drops: list[ProfileDropOut]
    # The podium of the Mixtape, from the 1st place to the last: empty if not made yet
    podium: list[PodiumSongOut]
    # The labeled songs of the Mixtape, in the order of the labels: empty if none yet
    labeled_songs: list[LabeledSongOut]
    # The badges given from the dashboard, the most recently given first: empty if none yet
    badges: list[UserBadgeOut]
    # Number of users who follow this user, and number of users this user follows
    followers_count: int
    following_count: int
    # True when the logged-in user follows this user
    is_followed: bool
    # True when this user follows the logged-in user ("Suivre en retour" in the app)
    follows_you: bool
    # True on the logged-in user's own profile: no "Suivre" button
    is_me: bool


class ProfileUpdate(BaseModel):
    """What the user can change on their own profile. A field left out stays the same."""

    # New display name; an empty text gives back the username
    display_name: str | None = Field(default=None, max_length=30)
    # One of the 5 background colors
    avatar_color: AvatarColor | None = None
    # One of the 9 drop styles
    avatar_style: AvatarStyle | None = None
    # Color of the drop, background decoration, face and aura
    avatar_drop: AvatarDrop | None = None
    avatar_deco: AvatarDeco | None = None
    avatar_face: AvatarFace | None = None
    avatar_aura: AvatarAura | None = None