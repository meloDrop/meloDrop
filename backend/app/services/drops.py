"""Functions that create and read the song of the day."""

import random
from datetime import timedelta, timezone

from sqlalchemy.orm import Session

from app.config import settings
from app.exceptions import DeezerError, DropAlreadyActiveError, DropNotFoundError, NotDropOwnerError
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

# Talks to the public Deezer API
deezer_provider = DeezerProvider()


def get_active_drop(db: Session, user_id: int) -> Drop | None:
    """Return the user's drop that has not expired yet, or None."""
    # A drop is active if it ends later than now
    return db.query(Drop).filter(Drop.user_id == user_id, Drop.expires_at > utc_now()).first()

def get_album_genre(album_id: int) -> str | None:
    """Return the name of the album's first genre, or None (ADR-0017)."""
    # The genre is only informative: a Deezer error must not block the drop
    try:
        album = deezer_provider.get_album(album_id)
    except DeezerError:
        return None

    # Some albums have no genre
    genres = album.get("genres", {}).get("data", [])
    if not genres:
        return None
    return genres[0]["name"]


def find_or_create_song(db: Session, deezer_track_id: int) -> Song:
    """Find the song in the database, or create it from Deezer (ADR-0014)."""
    # Step 1: look for the song with its Deezer id (no call to Deezer)
    song = db.query(Song).filter(Song.deezer_track_id == deezer_track_id).first()
    if song is not None:
        return song

    # Step 2: ask Deezer for the song, to get its ISRC
    track = deezer_provider.get_track(deezer_track_id)
    isrc = track.get("isrc") or None

    # Look for the song with its ISRC (same song, other Deezer id)
    if isrc:
        song = db.query(Song).filter(Song.isrc == isrc).first()
        if song is not None:
            return song

    # Step 3: the song is new, so we create it
    song = Song(
        isrc=isrc,
        title=track["title"],
        artist_name=track["artist"]["name"],
        cover_url=track["album"]["cover_big"],
        genre=get_album_genre(track["album"]["id"]),
        deezer_track_id=track["id"],
    )
    db.add(song)

    # Get the song id now, but do not save yet
    db.flush()
    return song


def create_drop(db: Session, user_id: int, deezer_track_id: int, genre_ids: list[int]) -> DropOut:
    """Drop the song of the day for this user."""
    # Only one drop at a time
    if get_active_drop(db, user_id) is not None:
        raise DropAlreadyActiveError("drop_already_active")

    # Get the song (from the database or from Deezer)
    song = find_or_create_song(db, deezer_track_id)

    # The drop lasts 24 hours; rand_key is used for the random feed
    drop = Drop(
        user_id=user_id,
        song_id=song.id,
        expires_at=utc_now() + timedelta(hours=24),
        rand_key=random.random(),
    )
    db.add(drop)

    # Get the drop id now, but do not save yet
    db.flush()

    # Keep only the genres that exist
    genres = db.query(Genre).filter(Genre.id.in_(genre_ids)).all()

    # Add each genre to the drop
    for genre in genres:
        db.add(DropGenre(drop_id=drop.id, genre_id=genre.id))

    # Save the song, the drop and the genres together
    db.commit()

    # Send the drop back, with the date in UTC
    return DropOut(
        song_id=song.id,
        title=song.title,
        artist=song.artist_name,
        cover_url=song.cover_url,
        genre=song.genre,
        genres=[GenreOut(id=genre.id, label=genre.label) for genre in genres],
        expires_at=drop.expires_at.replace(tzinfo=timezone.utc),
    )


def get_my_drop(db: Session, user_id: int) -> MyDropOut | None:
    """Return the user's current drop with its stats, or None if there is none."""
    # No active drop: return None
    drop = get_active_drop(db, user_id)
    if drop is None:
        return None

    # Get the song of the drop
    song = db.get(Song, drop.song_id)

    # Get the genres of the drop
    genres = (
        db.query(Genre)
        .join(DropGenre, DropGenre.genre_id == Genre.id)
        .filter(DropGenre.drop_id == drop.id)
        .all()
    )

    # Count the likes and the genre reports of the drop
    saves_count = db.query(Save).filter(Save.drop_id == drop.id).count()
    reports_count = db.query(GenreReport).filter(GenreReport.drop_id == drop.id).count()

    # Send the drop back with its stats
    return MyDropOut(
        id=drop.id,
        song_id=song.id,
        title=song.title,
        artist=song.artist_name,
        cover_url=song.cover_url,
        genre=song.genre,
        genres=[GenreOut(id=genre.id, label=genre.label) for genre in genres],
        expires_at=drop.expires_at.replace(tzinfo=timezone.utc),
        saves_count=saves_count,
        genre_reported=reports_count >= settings.GENRE_REPORT_THRESHOLD_TEST,
    )


def update_genres(db: Session, user_id: int, drop_id: int, genre_ids: list[int]) -> MyDropOut:
    """Replace the genres of the user's current drop and clear its genre reports."""
    # The drop must exist and must not be expired
    drop = db.get(Drop, drop_id)
    if drop is None or drop.expires_at <= utc_now():
        raise DropNotFoundError("drop_not_found")

    # Only the author can change the genres
    if drop.user_id != user_id:
        raise NotDropOwnerError("not_drop_owner")

    # Delete the old genres
    db.query(DropGenre).filter(DropGenre.drop_id == drop.id).delete()

    # Delete the genre reports, because the genres are fixed
    db.query(GenreReport).filter(GenreReport.drop_id == drop.id).delete()

    # Keep only the genres that exist
    genres = db.query(Genre).filter(Genre.id.in_(genre_ids)).all()

    # Add each new genre to the drop
    for genre in genres:
        db.add(DropGenre(drop_id=drop.id, genre_id=genre.id))

    # Save everything together
    db.commit()

    # Send the updated drop back
    return get_my_drop(db, user_id)