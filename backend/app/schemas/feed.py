"""Data sent back by the feed routes."""

from pydantic import BaseModel

from app.schemas.users import UserBriefOut


class FeedItem(BaseModel):
    """A song shown as a card in the Discover feed (ADR-0021).

    When several users dropped the same song, the card shows it once:
    id and author_* are those of the first drop, droppers lists everyone.
    """

    # Drop sent with /save, /pick and /skip: the swipe counts for every drop of the song
    id: int
    song_id: int
    title: str
    artist: str
    cover_url: str | None = None
    genre: str | None = None
    # First user in droppers: shown alone when only one user dropped the song
    author_id: int
    author_username: str
    # Users who liked the drops of the song online now, each one counted once
    saves_count: int
    # True when the logged-in user follows one of the droppers: "Abonné" on the card
    is_followed: bool = False
    # Everyone who dropped the song and is online now: the followed users first,
    # then from the first drop to the last one
    droppers: list[UserBriefOut] = []
    # True when the song is already in the user's "Mes likes": someone dropped it again.
    # A like still counts for the new droppers, and the song is not added twice
    already_liked: bool = False
