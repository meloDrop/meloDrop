"""Functions that build the Discover feed."""

import random

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.models.drop import Drop
from app.models.drop_genre import DropGenre
from app.models.genre import Genre
from app.models.save import Save
from app.models.skip import Skip
from app.models.song import Song
from app.models.user import User
from app.schemas.feed import FeedItem
from app.schemas.genres import GenreOut
from app.utils.time import utc_now

# Number of drops sent to the app at once
FEED_SIZE = 10


def build_item(db: Session, drop: Drop) -> FeedItem:
    """Build the feed card of a drop."""
    # Get the song and the author of the drop
    song = db.get(Song, drop.song_id)
    author = db.get(User, drop.user_id)

    # Get the genres of the drop
    genres = (
        db.query(Genre)
        .join(DropGenre, DropGenre.genre_id == Genre.id)
        .filter(DropGenre.drop_id == drop.id)
        .all()
    )

    # Count the likes of the drop
    saves_count = db.query(Save).filter(Save.drop_id == drop.id).count()

    # Build the card sent to the app
    return FeedItem(
        id=drop.id,
        song_id=song.id,
        title=song.title,
        artist=song.artist_name,
        cover_url=song.cover_url,
        genres=[GenreOut(id=genre.id, label=genre.label) for genre in genres],
        author_id=author.id,
        author_username=author.username,
        saves_count=saves_count,
    )


def get_feed(db: Session, user_id: int) -> list[FeedItem]:
    """Return up to 10 random drops the user has not seen yet."""
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

    # Pick a random point, then read the drops after it
    start = random.random()
    drops = query.filter(Drop.rand_key >= start).order_by(Drop.rand_key).limit(FEED_SIZE).all()

    # Not enough drops: read again from the start
    if len(drops) < FEED_SIZE:
        missing = FEED_SIZE - len(drops)
        drops += query.filter(Drop.rand_key < start).order_by(Drop.rand_key).limit(missing).all()

    # Turn each drop into a feed card
    items = []
    for drop in drops:
        items.append(build_item(db, drop))
    return items