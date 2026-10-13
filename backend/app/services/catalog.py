"""Functions that search tracks on Deezer."""

from app.providers.deezer import DeezerProvider
from app.schemas.catalog import TrackResult

# Talks to the public Deezer API
deezer_provider = DeezerProvider()


def search_tracks(query: str) -> list[TrackResult]:
    """Search tracks on Deezer and return them in meloDrop's format."""
    # Ask Deezer for the tracks (a DeezerError goes up to the router)
    results = deezer_provider.search(query)

    # Turn each Deezer result into a simple TrackResult
    tracks = []
    for track in results:
        tracks.append(
            TrackResult(
                deezer_track_id=track["id"],
                title=track["title"],
                artist=track["artist"]["name"],
                cover_url=track["album"]["cover_medium"],
                # No preview for this track: None instead of an empty string
                preview_url=track.get("preview") or None,
            )
        )
    return tracks