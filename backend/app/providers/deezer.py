"""Deezer implementation of the StreamingProvider interface."""

import requests

from app.config import settings
from app.exceptions import DeezerError, TrackNotFoundError
from app.providers.base import StreamingProvider

BASE_URL = "https://api.deezer.com"
TIMEOUT = 5
NOT_FOUND_CODE = 800

# V2 only: used by the OAuth and playlist methods below
OAUTH_URL = "https://connect.deezer.com/oauth"
PLAYLIST_NAME = "meloDrop"


def call_public_api(path: str, params: dict | None = None) -> dict:
    """Call a public Deezer endpoint and raise an error if Deezer reports one."""
    try:
        response = requests.get(f"{BASE_URL}{path}", params=params, timeout=TIMEOUT)
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

    # =========================================================================
    # V2 ONLY - NOT USED IN V1
    #
    # V1 has no streaming account linking (ADR-0011, ADR-0012), and Deezer has
    # suspended the creation of new developer apps (ADR-0010), so the methods
    # below cannot run today. They are kept because the StreamingProvider
    # interface requires them, and for a possible V2.
    # =========================================================================

    def add_favorite(self, access_token: str, track_id: str, playlist_id: str) -> None:
        """Add a track to the user's meloDrop playlist on Deezer."""
        response = requests.post(
            f"{BASE_URL}/playlist/{playlist_id}/tracks",
            params={
                "access_token": access_token,
                "songs": track_id,
                "request_method": "POST",
            },
        )
        response.raise_for_status()

    def exchange_code(self, code: str) -> dict:
        """Exchange an OAuth code for a Deezer access token."""
        response = requests.get(
            f"{OAUTH_URL}/access_token.php",
            params={
                "app_id": settings.DEEZER_APP_ID,
                "secret": settings.DEEZER_APP_SECRET,
                "code": code,
                "output": "json",
            },
        )
        response.raise_for_status()
        return response.json()

    def get_current_user(self, access_token: str) -> dict:
        """Return the Deezer profile of the authenticated user."""
        response = requests.get(f"{BASE_URL}/user/me", params={"access_token": access_token})
        response.raise_for_status()
        return response.json()

    def create_playlist(self, access_token: str) -> str:
        """Create the meloDrop playlist on Deezer and return its id."""
        response = requests.post(
            f"{BASE_URL}/user/me/playlists",
            params={
                "access_token": access_token,
                "title": PLAYLIST_NAME,
                "request_method": "POST",
            },
        )
        response.raise_for_status()
        return str(response.json()["id"])

    def refresh_access_token(self, refresh_token: str) -> dict:
        """Not needed: Deezer tokens do not expire with offline_access."""
        raise NotImplementedError("Deezer tokens do not expire")