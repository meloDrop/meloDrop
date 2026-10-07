"""Business logic of the catalog module."""

from app.providers.deezer import DeezerProvider
from app.schemas.catalog import TrackResult

deezer_provider = DeezerProvider()


def search_tracks(query: str) -> list[TrackResult]:
    """Search tracks on Deezer and return them in meloDrop's format."""
    results = deezer_provider.search(query)
    tracks = []
    for track in results:
        tracks.append(
            TrackResult(
                deezer_track_id=track["id"],
                title=track["title"],
                artist=track["artist"]["name"],
                cover_url=track["album"]["cover_medium"],
                preview_url=track.get("preview") or None,
            )
        )
    return tracks