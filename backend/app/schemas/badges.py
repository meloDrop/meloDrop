"""Data received and sent by the badge routes."""

from datetime import datetime
from typing import Literal

from pydantic import BaseModel, Field

# Icons a badge can have. The app (BadgeIcons.kt) and the dashboard draw each one
BadgeIcon = Literal[
    "drop",
    "flame",
    "star",
    "heart",
    "headphones",
    "calendar",
    "bolt",
    "crown",
    "trophy",
    "vinyl",
    "mic",
    "note",
]

# Colors a badge can have: the 5 pastels of the avatars, or "spectrum" for all of them
BadgeColor = Literal["lavender", "peach", "butter", "mint", "rose", "spectrum"]


class BadgeCreate(BaseModel):
    """A new badge, created from the dashboard."""

    # Spaces around the texts are removed before their length is checked
    model_config = {"str_strip_whitespace": True}

    # Shown under the badge on the Mixtape, like "Testeur de la 1re heure"
    name: str = Field(min_length=1, max_length=40)
    # Why the users got it, shown when the badge is tapped
    description: str = Field(min_length=1, max_length=200)
    icon: BadgeIcon
    color: BadgeColor


class BadgeUpdate(BaseModel):
    """What can change on a badge. A field left out stays the same."""

    model_config = {"str_strip_whitespace": True}

    name: str | None = Field(default=None, min_length=1, max_length=40)
    description: str | None = Field(default=None, min_length=1, max_length=200)
    icon: BadgeIcon | None = None
    color: BadgeColor | None = None


class BadgeHolderOut(BaseModel):
    """A user who has the badge."""

    user_id: int
    username: str
    # When the badge was given, in UTC
    awarded_at: datetime


class BadgeOut(BaseModel):
    """A badge as the admin sees it, with the users who have it."""

    id: int
    name: str
    description: str
    icon: str
    color: str
    # When the badge was created, in UTC
    created_at: datetime
    # The users who have it, the most recent first
    holders: list[BadgeHolderOut]


class UserBadgeOut(BaseModel):
    """A badge on a profile: the badge, and when the user got it."""

    # Id of the badge (badges table)
    id: int
    name: str
    description: str
    # Codes of the icon and of the color: the app draws an unknown code with its default one
    icon: str
    color: str
    # When the badge was given, in UTC
    awarded_at: datetime
