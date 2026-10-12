"""Functions that create and read the song of the day."""

import random
from datetime import timedelta, timezone

from sqlalchemy.orm import Session

from app.config import settings
from app.exceptions import DropAlreadyActiveError
from app.models.drop import Drop
from app.models.drop_genre import DropGenre
from app.models.genre import Genre
from app.models.genre_report import GenreReport
from app.models.save import Save
from app.models.song import Song
from app.providers.deezer import DeezerProvider
from app.schemas.drops import DropOut, MyDropOut
from app.schemas.genres import GenreOut
from app.utils.time import utc_now

deezer_provider = DeezerProvider()


def get_active_drop(db: Session, user_id: int) -> Drop | None:
    """Return the user's drop that has not expired yet, or None."""
    return db.query(Drop).filter(Drop.user_id == user_id, Drop.expires_at > utc_now()).first()


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
    if get_active_drop(db, user_id) is not None:
        raise DropAlreadyActiveError("drop_already_active")

    song = find_or_create_song(db, deezer_track_id)

    drop = Drop(
        user_id=user_id,
        song_id=song.id,
        expires_at=utc_now() + timedelta(hours=24),
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


def get_my_drop(db: Session, user_id: int) -> MyDropOut | None:
    """Return the user's current drop with its stats, or None if there is none."""
    drop = get_active_drop(db, user_id)
    if drop is None:
        return None

    song = db.get(Song, drop.song_id)
    genres = (
        db.query(Genre)
        .join(DropGenre, DropGenre.genre_id == Genre.id)
        .filter(DropGenre.drop_id == drop.id)
        .all()
    )
    saves_count = db.query(Save).filter(Save.drop_id == drop.id).count()
    reports_count = db.query(GenreReport).filter(GenreReport.drop_id == drop.id).count()

    return MyDropOut(
        id=drop.id,
        song_id=song.id,
        title=song.title,
        artist=song.artist_name,
        cover_url=song.cover_url,
        genres=[GenreOut(id=genre.id, label=genre.label) for genre in genres],
        expires_at=drop.expires_at.replace(tzinfo=timezone.utc),
        saves_count=saves_count,
        genre_reported=reports_count >= settings.GENRE_REPORT_THRESHOLD_TEST,
    )