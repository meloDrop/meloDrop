"""Deezer implementation of the StreamingProvider interface."""

import requests

from app.config import settings
from app.exceptions import DeezerError, TrackNotFoundError
from app.providers.base import StreamingProvider

BASE_URL = "https://api.deezer.com"
TIMEOUT = 5
NOT_FOUND_CODE = 800
HEADERS = {"Accept-Language": "fr"}

# V2 only: used by the OAuth and playlist methods below
OAUTH_URL = "https://connect.deezer.com/oauth"
PLAYLIST_NAME = "meloDrop"


def call_public_api(path: str, params: dict | None = None) -> dict:
    """Call a public Deezer endpoint and raise an error if Deezer reports one."""
    try:
        response = requests.get(f"{BASE_URL}{path}", params=params, headers=HEADERS, timeout=TIMEOUT)
        response.raise_for_status()
        data = response.json()
    except requests.RequestException:
        raise DeezerError("deezer_unavailable")

    if "error" in data:
        if data["error"].get("code") == NOT_FOUND_CODE:
            raise TrackNotFoundError("track_not_found")
        raise DeezerError("deezer_error")
    return data


class DeezerProvider(StreamingProvider):
    """Talks to the Deezer API for search, track lookup, playlist and OAuth."""

    # V1: public API, no developer app and no user account needed

    def search(self, query: str) -> list[dict]:
        """Search tracks on Deezer matching the query."""
        return call_public_api("/search", {"q": query})["data"]

    def get_track(self, track_id: str) -> dict:
        """Return a Deezer track's details, including its ISRC and preview URL."""
        return call_public_api(f"/track/{track_id}")

    def get_album(self, album_id: int) -> dict:
        """Return a Deezer album's details, including its genres (ADR-0017)."""
        return call_public_api(f"/album/{album_id}")

