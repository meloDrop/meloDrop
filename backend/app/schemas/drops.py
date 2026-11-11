"""Data received and sent by the drops routes."""

from datetime import datetime

from pydantic import BaseModel


class DropCreate(BaseModel):
    """Data needed to drop the song of the day."""

    deezer_track_id: int


class DropOut(BaseModel):
    """A drop as shown in the app."""

    song_id: int
    title: str
    artist: str
    cover_url: str | None = None
    genre: str | None = None
    expires_at: datetime


class MyDropOut(DropOut):
    """The logged-in user's current drop, with its stats."""

    id: int
    # Likes received, picks included
    saves_count: int
    # Picks received (part of saves_count)
    picks_count: int
    # Users who swiped the drop: likes, picks and skips
    views_count: int


class DropLikerOut(BaseModel):
    """A user who liked one of my drops, as shown in the list of likes and picks."""

    # Used to open the profile (GET /users/{id}/profile)
    id: int
    username: str
    # Public name, or null: the app then shows the username
    display_name: str | None = None
    avatar_color: str
    avatar_style: str
    # Cosmetics: color of the drop, background decoration, face and aura
    avatar_drop: str = "night"
    avatar_deco: str = "plain"
    avatar_face: str = "none"
    avatar_aura: str = "none"
    # True: the like was a pick (up swipe or star button)
    is_pick: bool
