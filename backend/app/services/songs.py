"""Functions that read a song: its poster, and a fresh preview link."""

from datetime import date

from sqlalchemy.orm import Session

from app.exceptions import DeezerError, SongNotFoundError
from app.models.drop import Drop
from app.models.liked_song import LikedSong
from app.models.song import Song
from app.providers.deezer import DeezerProvider
from app.schemas.songs import PreviewOut, SongDetailOut, SongStatsOut

# Talks to the public Deezer API
deezer_provider = DeezerProvider()


def get_preview(db: Session, song_id: int) -> PreviewOut:
    """Return a fresh Deezer preview link for a song stored in meloDrop."""
    # The song must exist in the database
    song = db.get(Song, song_id)
    if song is None:
        raise SongNotFoundError("song_not_found")

    # Ask Deezer again: preview links expire after about 15 minutes
    track = deezer_provider.get_track(song.deezer_track_id)

    # No preview for this track: None instead of an empty string
    return PreviewOut(preview_url=track.get("preview") or None)


def get_song(db: Session, song_id: int) -> SongDetailOut:
    """Return everything shown on the poster of a song: its Deezer details and its meloDrop counters."""
    # The song must exist in the database
    song = db.get(Song, song_id)
    if song is None:
        raise SongNotFoundError("song_not_found")

    # Deezer gives the album, the date and the length. Without an answer,
    # the poster still shows what meloDrop knows: no error for the user
    track = {}
    if song.deezer_track_id is not None:
        try:
            track = deezer_provider.get_track(song.deezer_track_id)
        except DeezerError:
            track = {}
    album = track.get("album") or {}

    return SongDetailOut(
        song_id=song.id,
        deezer_track_id=song.deezer_track_id,
        title=song.title,
        artist=song.artist_name,
        cover_url=song.cover_url,
        genre=song.genre,
        album=album.get("title") or None,
        release_date=read_release_date(track.get("release_date")),
        duration=track.get("duration") or None,
        explicit=bool(track["explicit_lyrics"]) if "explicit_lyrics" in track else None,
        stats=get_song_stats(db, song.id),
    )


def get_song_stats(db: Session, song_id: int) -> SongStatsOut:
    """Count the drops, the likes and the picks of a song, all the users together."""
    drops_count = db.query(Drop).filter(Drop.song_id == song_id).count()
    likes_count = db.query(LikedSong).filter(LikedSong.song_id == song_id).count()
    picks_count = (
        db.query(LikedSong)
        .filter(LikedSong.song_id == song_id, LikedSong.is_pick.is_(True))
        .count()
    )
    return SongStatsOut(drops_count=drops_count, likes_count=likes_count, picks_count=picks_count)


def read_release_date(value: str | None) -> date | None:
    """Turn the Deezer date into a date. Deezer sometimes sends "0000-00-00": None then."""
    if not value:
        return None
    try:
        return date.fromisoformat(value)
    except ValueError:
        return None
