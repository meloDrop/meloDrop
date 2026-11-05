"""Data received and sent by the podium routes."""

from pydantic import BaseModel, Field

# Number of places on a podium
PODIUM_SIZE = 3


class PodiumUpdate(BaseModel):
    """The whole new podium: the Deezer ids of its songs, from the 1st place to the last."""

    # The first id goes to the 1st place. An empty list empties the podium
    deezer_track_ids: list[int] = Field(max_length=PODIUM_SIZE)


class PodiumSongOut(BaseModel):
    """A song on a podium, with its place."""

    # 1, 2 or 3
    place: int
    song_id: int
    title: str
    artist: str
    cover_url: str | None = None