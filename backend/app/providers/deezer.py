"""Deezer implementation of the StreamingProvider interface."""

import requests

from app.config import settings
from app.providers.base import StreamingProvider

BASE_URL = "https://api.deezer.com"
OAUTH_URL = "https://connect.deezer.com/oauth"
PLAYLIST_NAME = "meloDrop"


class DeezerProvider(StreamingProvider):
    """Talks to the Deezer API for search, track lookup, playlist and OAuth."""

    def search(self, query: str) -> list[dict]:
        """Search tracks on Deezer matching the query."""
        response = requests.get(f"{BASE_URL}/search", params={"q": query})
        response.raise_for_status()
        return response.json()["data"]

    def get_track(self, track_id: str) -> dict:
        """Return a Deezer track's details, including its ISRC and preview URL."""
        response = requests.get(f"{BASE_URL}/track/{track_id}")
        response.raise_for_status()
        return response.json()

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
