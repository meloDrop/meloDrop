"""Functions that search tracks on Deezer."""

from sqlalchemy.orm import Session

from app.exceptions import SongNotFoundError
from app.models.song import Song
from app.providers.deezer import DeezerProvider
from app.schemas.songs import PreviewOut

deezer_provider = DeezerProvider()


def get_preview(db: Session, song_id: int) -> PreviewOut:
    """Return a fresh Deezer preview link for a song stored in meloDrop."""
    song = db.get(Song, song_id)
    if song is None:
        raise SongNotFoundError("song_not_found")

    track = deezer_provider.get_track(song.deezer_track_id)
    return PreviewOut(preview_url=track.get("preview") or None)