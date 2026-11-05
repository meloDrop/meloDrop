"""Data received and sent by the badge routes."""

from datetime import datetime
from typing import Literal

from pydantic import BaseModel, Field, model_validator

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

# Rules of the automatic badges. The badge is given as soon as the number reaches the threshold:
# drops: drops shared. streak: days in a row with a drop (French days).
# likes_received: likes received, picks included. picks_received: picks received.
# like_rate: like rate received, in %, once the drops have 30 views
BadgeRule = Literal["drops", "streak", "likes_received", "picks_received", "like_rate"]

# Highest threshold: more would mean a badge nobody can get
MAX_THRESHOLD = 100000


def check_rule(rule: str | None, threshold: int | None) -> None:
    """A rule needs a threshold, and a rate can't go over 100 %."""
    if rule is not None and threshold is None:
        raise ValueError("threshold_required")
    if rule == "like_rate" and threshold is not None and threshold > 100:
        raise ValueError("rate_over_100")


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
    # Automatic badge: the rule and its threshold. Left out: given by hand only
    rule: BadgeRule | None = None
    threshold: int | None = Field(default=None, ge=1, le=MAX_THRESHOLD)

    @model_validator(mode="after")
    def check_threshold(self):
        """No threshold without a rule; a rule needs one."""
        check_rule(self.rule, self.threshold)
        if self.rule is None:
            self.threshold = None
        return self


class BadgeUpdate(BaseModel):
    """What can change on a badge. A field left out stays the same."""

    model_config = {"str_strip_whitespace": True}

    name: str | None = Field(default=None, min_length=1, max_length=40)
    description: str | None = Field(default=None, min_length=1, max_length=200)
    icon: BadgeIcon | None = None
    color: BadgeColor | None = None
    # Sent together. rule null: the badge goes back to "given by hand only"
    rule: BadgeRule | None = None
    threshold: int | None = Field(default=None, ge=1, le=MAX_THRESHOLD)

    @model_validator(mode="after")
    def check_threshold(self):
        """The rule and the threshold change together, or not at all."""
        if "threshold" in self.model_fields_set and "rule" not in self.model_fields_set:
            raise ValueError("rule_required")
        if "rule" in self.model_fields_set:
            check_rule(self.rule, self.threshold)
        return self


class BadgeAwardMany(BaseModel):
    """The users to give a badge to, chosen in the Badges tab."""

    user_ids: list[int] = Field(min_length=1, max_length=500)


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
    # Rule and threshold of an automatic badge, null for a badge given by hand
    rule: str | None = None
    threshold: int | None = None
    # When the badge was created, in UTC
    created_at: datetime
    # The users who have it, the most recent first
    holders: list[BadgeHolderOut]


class BadgeAwardOut(BaseModel):
    """Result of a badge given to several users at once."""

    # Ids of the users who got the badge now
    awarded: list[int]
    # Ids of the users who already had it: nothing changed for them
    already_had: list[int]
    # The badge, with all the users who have it now
    badge: BadgeOut


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
