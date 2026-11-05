"""Data received and sent by the labeled songs routes."""

from typing import Literal, get_args

from pydantic import BaseModel

# Codes of the labels, in the order of the Mixtape. The app shows the text,
# like "Le son qui me fait chialer" for "cry". A code removed from this list
# disappears from the profiles, without a migration
SongLabel = Literal[
    "feel_good",
    "party",
    "never_skip",
    "theme_song",
    "chills",
    "cry",
    "guilty_pleasure",
    "cant_stand",
]

# The same codes as a tuple, to sort the songs in this order
LABEL_ORDER = get_args(SongLabel)


class LabeledSongUpdate(BaseModel):
    """The song to put under a label: its Deezer id, from the search."""

    deezer_track_id: int


class LabeledSongOut(BaseModel):
    """A song of the Mixtape, with its label."""

    label: SongLabel
    song_id: int
    # Deezer id of the song: the app can send it back to keep the same song
    deezer_track_id: int
    title: str
    artist: str
    cover_url: str | None = None
