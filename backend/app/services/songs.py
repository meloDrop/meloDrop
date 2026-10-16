"""Functions that get a fresh preview link for a song."""

from sqlalchemy.orm import Session

from app.exceptions import SongNotFoundError
from app.models.song import Song
from app.providers.deezer import DeezerProvider
from app.schemas.songs import PreviewOut

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