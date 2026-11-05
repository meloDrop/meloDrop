"""Functions that build the Discover feed."""

import random

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.models.drop import Drop
from app.models.save import Save
from app.models.skip import Skip
from app.models.song import Song
from app.models.user import User
from app.schemas.feed import FeedItem
from app.services.follows import followed_ids
from app.utils.time import utc_now

# Number of drops sent to the app at once
FEED_SIZE = 10


def build_item(db: Session, drop: Drop, is_followed: bool = False) -> FeedItem:
    """Build the feed card of a drop. is_followed: the user follows its author."""
    # Get the song and the author of the drop
    song = db.get(Song, drop.song_id)
    author = db.get(User, drop.user_id)

    # Count the likes of the drop
    saves_count = db.query(Save).filter(Save.drop_id == drop.id).count()

    # Build the card sent to the app
    return FeedItem(
        id=drop.id,
        song_id=song.id,
        title=song.title,
        artist=song.artist_name,
        cover_url=song.cover_url,
        genre=song.genre,
        author_id=author.id,
        author_username=author.username,
        saves_count=saves_count,
        is_followed=is_followed,
    )


def get_feed(db: Session, user_id: int, follows_first: bool = True) -> list[FeedItem]:
    """Return up to 10 random drops the user has not seen yet.

    follows_first: the drops of the followed users come first, in a random order,
    then the others. False: all the drops are mixed, like before the follows.
    """
    # Drops the user already liked or skipped
    saved = select(Save.drop_id).where(Save.user_id == user_id)
    skipped = select(Skip.drop_id).where(Skip.user_id == user_id)

    # Active drops of other users, not seen yet
    query = db.query(Drop).filter(
        Drop.expires_at > utc_now(),
        Drop.user_id != user_id,
        Drop.id.not_in(saved),
        Drop.id.not_in(skipped),
    )

    # Users followed: their cards get "Abonné", and can come first
    followed = followed_ids(db, user_id)

    drops = []
    if follows_first and followed:
        # One drop at most per user, so this list stays short: shuffled in Python
        drops = query.filter(Drop.user_id.in_(list(followed))).all()
        random.shuffle(drops)
        drops = drops[:FEED_SIZE]
        # The rest of the batch comes from the other users
        query = query.filter(Drop.user_id.not_in(list(followed)))

    # The places left get random drops
    missing = FEED_SIZE - len(drops)
    if missing > 0:
        drops += pick_random(query, missing)

    # Turn each drop into a feed card
    items = []
    for drop in drops:
        items.append(build_item(db, drop, drop.user_id in followed))
    return items


def pick_random(query, count: int) -> list[Drop]:
    """Return up to count random drops of the query."""
    # Pick a random point, then read the drops after it
    start = random.random()
    drops = query.filter(Drop.rand_key >= start).order_by(Drop.rand_key).limit(count).all()

    # Not enough drops: read again from the start
    if len(drops) < count:
        missing = count - len(drops)
        drops += query.filter(Drop.rand_key < start).order_by(Drop.rand_key).limit(missing).all()
    return drops