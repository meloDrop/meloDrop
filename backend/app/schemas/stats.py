"""Data sent back by the stats module."""

from pydantic import BaseModel


class StatsOut(BaseModel):
    """The three counters of a profile."""

    # Drops shared by the user, expired ones included
    drops_count: int
    # Likes received on the user's drops (saves table)
    likes_received: int
    # Songs in the user's "Mes likes" list (liked_songs table, ADR-0013)
    likes_count: int