"""Data sent back by the songs routes."""

from datetime import date

from pydantic import BaseModel


class PreviewOut(BaseModel):
    """A fresh 30-second preview link for a song."""

    preview_url: str | None = None


class SongStatsOut(BaseModel):
    """What the song did on meloDrop, all the users together."""

    # Drops of this song, expired ones included
    drops_count: int
    # Users who have it in "Mes likes", picks included
    likes_count: int
    # Users who made it a pick
    picks_count: int


class SongDetailOut(BaseModel):
    """Everything shown on the poster of a song in the app."""

    song_id: int
    deezer_track_id: int | None = None
    title: str
    artist: str
    cover_url: str | None = None
    # Genre of the Deezer album, null when Deezer has none
    genre: str | None = None
    # From Deezer: null when Deezer does not answer or no longer has the track
    album: str | None = None
    # Release date of the track, like "2011-08-16"
    release_date: date | None = None
    # Length in seconds
    duration: int | None = None
    # True for explicit lyrics
    explicit: bool | None = None
    stats: SongStatsOut
