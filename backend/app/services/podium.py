"""Functions that read and change a user's podium."""

from sqlalchemy.orm import Session

from app.exceptions import DuplicateSongError
from app.models.podium_song import PodiumSong
from app.models.song import Song
from app.schemas.podium import PodiumSongOut
from app.services.drops import find_or_create_song


def get_podium(db: Session, user_id: int) -> list[PodiumSongOut]:
    """Return the user's podium, from the 1st place to the last."""
    rows = (
        db.query(PodiumSong)
        .filter(PodiumSong.user_id == user_id)
        .order_by(PodiumSong.place)
        .all()
    )

    podium = []
    for row in rows:
        # Get the song of this place
        song = db.get(Song, row.song_id)
        podium.append(
            PodiumSongOut(
                place=row.place,
                song_id=song.id,
                title=song.title,
                artist=song.artist_name,
                cover_url=song.cover_url,
            )
        )
    return podium


def set_podium(db: Session, user_id: int, deezer_track_ids: list[int]) -> list[PodiumSongOut]:
    """Replace the whole podium of the user, and return the new one."""
    # Find or create each song first: if Deezer fails, nothing is changed
    songs = []
    for deezer_track_id in deezer_track_ids:
        songs.append(find_or_create_song(db, deezer_track_id))

    # The same song only once. Two Deezer ids can be the same song (same ISRC),
    # so we compare the songs of meloDrop, not the Deezer ids
    song_ids = [song.id for song in songs]
    if len(set(song_ids)) != len(song_ids):
        raise DuplicateSongError("duplicate_song")

    # Remove the old podium. delete() runs right away, so the new places
    # do not clash with the old ones (uq_podium_songs_user_place)
    db.query(PodiumSong).filter(PodiumSong.user_id == user_id).delete()

    # The first song goes to the 1st place
    for place, song in enumerate(songs, start=1):
        db.add(PodiumSong(user_id=user_id, song_id=song.id, place=place))

    # Save the new podium in one go
    db.commit()
    return get_podium(db, user_id)