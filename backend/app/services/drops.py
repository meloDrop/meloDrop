"""Functions that create and read the song of the day."""

import random
from datetime import datetime, timedelta, timezone

from sqlalchemy.orm import Session

from app.exceptions import DropAlreadyActiveError
from app.models.drop import Drop
from app.models.drop_genre import DropGenre
from app.models.genre import Genre
from app.models.song import Song
from app.providers.deezer import DeezerProvider
from app.schemas.drops import DropOut
from app.schemas.genres import GenreOut

deezer_provider = DeezerProvider()


def find_or_create_song(db: Session, deezer_track_id: int) -> Song:
    """Find the song in the database, or create it from Deezer (ADR-0014)."""
    song = db.query(Song).filter(Song.deezer_track_id == deezer_track_id).first()
    if song is not None:
        return song

    track = deezer_provider.get_track(deezer_track_id)
    isrc = track.get("isrc") or None
    if isrc:
        song = db.query(Song).filter(Song.isrc == isrc).first()
        if song is not None:
            return song

    song = Song(
        isrc=isrc,
        title=track["title"],
        artist_name=track["artist"]["name"],
        cover_url=track["album"]["cover_big"],
        deezer_track_id=track["id"],
    )
    db.add(song)
    db.flush()
    return song


def create_drop(db: Session, user_id: int, deezer_track_id: int, genre_ids: list[int]) -> DropOut:
    """Drop the song of the day for this user."""
    now = datetime.now(timezone.utc).replace(tzinfo=None)

    # One drop at a time: the previous one must have expired
    active_drop = db.query(Drop).filter(Drop.user_id == user_id, Drop.expires_at > now).first()
    if active_drop is not None:
        raise DropAlreadyActiveError("drop_already_active")

    song = find_or_create_song(db, deezer_track_id)

    drop = Drop(
        user_id=user_id,
        song_id=song.id,
        expires_at=now + timedelta(hours=24),
        rand_key=random.random(),
    )
    db.add(drop)
    db.flush()

    # Only the genres that exist in the closed list are kept
    genres = db.query(Genre).filter(Genre.id.in_(genre_ids)).all()
    for genre in genres:
        db.add(DropGenre(drop_id=drop.id, genre_id=genre.id))
    db.commit()

    return DropOut(
        song_id=song.id,
        title=song.title,
        artist=song.artist_name,
        cover_url=song.cover_url,
        genres=[GenreOut(id=genre.id, label=genre.label) for genre in genres],
        expires_at=drop.expires_at.replace(tzinfo=timezone.utc),
    )