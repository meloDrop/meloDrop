"""Functions that build the Discover feed: one card per song (ADR-0021)."""

import random
from datetime import timedelta

from sqlalchemy import distinct, func, select
from sqlalchemy.orm import Session

from app.models.drop import Drop
from app.models.liked_song import LikedSong
from app.models.save import Save
from app.models.skip import Skip
from app.models.song import Song
from app.models.user import User
from app.schemas.feed import FeedItem
from app.schemas.users import UserBriefOut
from app.services.follows import followed_ids
from app.utils.time import utc_now

# Number of songs sent to the app at once
FEED_SIZE = 10

# A song skipped stays out of the feed as long as it is in "Mes skips"
SKIPPED_DAYS = 7


def hidden_song_ids(db: Session, user_id: int) -> set[int]:
    """Songs the feed never shows to the user.

    - already in "Mes likes": the user knows them, the card would come back for nothing;
    - skipped in the last 7 days: they stay in "Mes skips", where a mistake can be fixed;
    - dropped by the user and still online: the user is one of the droppers.
    """
    now = utc_now()

    liked = db.scalars(select(LikedSong.song_id).where(LikedSong.user_id == user_id))

    skipped = db.scalars(
        select(Drop.song_id)
        .join(Skip, Skip.drop_id == Drop.id)
        .where(Skip.user_id == user_id, Skip.created_at >= now - timedelta(days=SKIPPED_DAYS))
    )

    my_drops = db.scalars(
        select(Drop.song_id).where(Drop.user_id == user_id, Drop.expires_at > now)
    )

    return set(liked) | set(skipped) | set(my_drops)


def count_likers(db: Session, drop_ids: list[int]) -> int:
    """Number of users who liked at least one of these drops, picks included."""
    return (
        db.query(func.count(distinct(Save.user_id)))
        .filter(Save.drop_id.in_(drop_ids))
        .scalar()
    )


def build_item(db: Session, drops: list[Drop], followed: set[int]) -> FeedItem:
    """Build the card of a song from its drops not seen yet, the oldest drop first.

    followed: ids of the users followed by the logged-in user.
    """
    song = db.get(Song, drops[0].song_id)

    # The followed users first. sorted keeps the order of the drops inside each part
    drops = sorted(drops, key=lambda drop: drop.user_id not in followed)

    droppers = []
    for drop in drops:
        author = db.get(User, drop.user_id)
        droppers.append(UserBriefOut.model_validate(author))

    # The first drop of the list is the one shown alone and sent with the swipe
    first = drops[0]

    return FeedItem(
        id=first.id,
        song_id=song.id,
        title=song.title,
        artist=song.artist_name,
        cover_url=song.cover_url,
        genre=song.genre,
        author_id=droppers[0].id,
        author_username=droppers[0].username,
        saves_count=count_likers(db, [drop.id for drop in drops]),
        is_followed=first.user_id in followed,
        droppers=droppers,
    )


def get_feed(db: Session, user_id: int, follows_first: bool = True) -> list[FeedItem]:
    """Return up to 10 random songs dropped by other users, not seen yet by the user.

    follows_first: the songs dropped by a followed user come first, in a random order,
    then the others. False: all the songs are mixed.
    """
    # Drops the user already liked or skipped
    saved = select(Save.drop_id).where(Save.user_id == user_id)
    skipped = select(Skip.drop_id).where(Skip.user_id == user_id)

    # Active drops of the other users, not seen yet, the oldest first.
    # One drop at most per user is online, so this list stays short
    drops = (
        db.query(Drop)
        .filter(
            Drop.expires_at > utc_now(),
            Drop.user_id != user_id,
            Drop.id.not_in(saved),
            Drop.id.not_in(skipped),
        )
        .order_by(Drop.created_at, Drop.id)
        .all()
    )

    # The drops of each song together: one card per song
    hidden = hidden_song_ids(db, user_id)
    drops_by_song = {}
    for drop in drops:
        if drop.song_id in hidden:
            continue
        drops_by_song.setdefault(drop.song_id, []).append(drop)

    # One chance per song, however many users dropped it: a hit does not come first
    songs = list(drops_by_song.values())
    random.shuffle(songs)

    # Users followed: their songs can come first, and their cards get "Abonné"
    followed = followed_ids(db, user_id)
    if follows_first and followed:
        # False comes before True: the songs with a followed dropper first.
        # sort keeps the random order inside each part
        songs.sort(key=lambda song_drops: not any(drop.user_id in followed for drop in song_drops))

    # Turn each song into a feed card
    items = []
    for song_drops in songs[:FEED_SIZE]:
        items.append(build_item(db, song_drops, followed))
    return items
