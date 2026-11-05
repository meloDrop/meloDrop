"""Functions that read and change the labeled songs of a Mixtape."""

from sqlalchemy.orm import Session

from app.exceptions import LabeledSongNotFoundError
from app.models.labeled_song import LabeledSong
from app.models.song import Song
from app.schemas.labeled_songs import LABEL_ORDER, LabeledSongOut
from app.services.drops import find_or_create_song


def get_labeled_songs(db: Session, user_id: int) -> list[LabeledSongOut]:
    """Return the user's labeled songs, in the order of the labels."""
    rows = db.query(LabeledSong).filter(LabeledSong.user_id == user_id).all()

    labeled_songs = []
    for row in rows:
        # A label removed from the list is not shown any more
        if row.label not in LABEL_ORDER:
            continue

        song = db.get(Song, row.song_id)
        labeled_songs.append(
            LabeledSongOut(
                label=row.label,
                song_id=song.id,
                deezer_track_id=song.deezer_track_id,
                title=song.title,
                artist=song.artist_name,
                cover_url=song.cover_url,
            )
        )

    # The order of the list of labels, not the order of creation
    labeled_songs.sort(key=lambda labeled: LABEL_ORDER.index(labeled.label))
    return labeled_songs


def set_labeled_song(db: Session, user_id: int, label: str, deezer_track_id: int) -> list[LabeledSongOut]:
    """Put a song under a label, or replace the one already there, and return all the labeled songs."""
    # Find or create the song first: if Deezer fails, nothing is changed
    song = find_or_create_song(db, deezer_track_id)

    row = (
        db.query(LabeledSong)
        .filter(LabeledSong.user_id == user_id, LabeledSong.label == label)
        .first()
    )
    if row is None:
        db.add(LabeledSong(user_id=user_id, song_id=song.id, label=label))
    else:
        # One song per label: the new one replaces the old one
        row.song_id = song.id

    db.commit()
    return get_labeled_songs(db, user_id)


def remove_labeled_song(db: Session, user_id: int, label: str) -> None:
    """Remove the song of a label from the user's Mixtape."""
    row = (
        db.query(LabeledSong)
        .filter(LabeledSong.user_id == user_id, LabeledSong.label == label)
        .first()
    )
    if row is None:
        raise LabeledSongNotFoundError("labeled_song_not_found")

    db.delete(row)
    db.commit()
