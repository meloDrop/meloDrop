"""Spotify implementation of the StreamingProvider interface."""

import time

import requests

from app.config import settings
from app.providers.base import StreamingProvider

API_URL = "https://api.spotify.com/v1"
TOKEN_URL = "https://accounts.spotify.com/api/token"
PLAYLIST_NAME = "meloDrop"


class SpotifyProvider(StreamingProvider):
    """Talks to the Spotify Web API for search, track lookup, playlist and OAuth."""

    def __init__(self):
        """Start without an app token: it is fetched on the first call."""
        self.app_token = None
        self.app_token_expires_at = 0

    def get_app_token(self) -> str:
        """Return an app token (client credentials), renewed one minute before it expires."""
        if self.app_token is None or time.time() > self.app_token_expires_at - 60:
            response = requests.post(
                TOKEN_URL,
                data={"grant_type": "client_credentials"},
                auth=(settings.SPOTIFY_CLIENT_ID, settings.SPOTIFY_CLIENT_SECRET),
            )
            response.raise_for_status()
            token_data = response.json()
            self.app_token = token_data["access_token"]
            self.app_token_expires_at = time.time() + token_data["expires_in"]
        return self.app_token

    def search(self, query: str) -> list[dict]:
        """Search tracks on Spotify (10 results max)."""
        response = requests.get(
            f"{API_URL}/search",
            params={"q": query, "type": "track", "limit": 10},
            headers={"Authorization": f"Bearer {self.get_app_token()}"},
        )
        response.raise_for_status()
        return response.json()["tracks"]["items"]

    def get_track(self, track_id: str) -> dict:
        """Return a Spotify track, with its ISRC in external_ids."""
        response = requests.get(
            f"{API_URL}/tracks/{track_id}",
            headers={"Authorization": f"Bearer {self.get_app_token()}"},
        )
        response.raise_for_status()
        return response.json()

    def add_favorite(self, access_token: str, track_id: str, playlist_id: str) -> None:
        """Add a track to the user's meloDrop playlist on Spotify."""
        response = requests.post(
            f"{API_URL}/playlists/{playlist_id}/items",
            json={"uris": [f"spotify:track:{track_id}"]},
            headers={"Authorization": f"Bearer {access_token}"},
        )
        response.raise_for_status()

    def exchange_code(self, code: str) -> dict:
        """Exchange an OAuth code for an access token and a refresh token."""
        response = requests.post(
            TOKEN_URL,
            data={
                "grant_type": "authorization_code",
                "code": code,
                "redirect_uri": settings.SPOTIFY_REDIRECT_URI,
            },
            auth=(settings.SPOTIFY_CLIENT_ID, settings.SPOTIFY_CLIENT_SECRET),
        )
        response.raise_for_status()
        return response.json()

    def refresh_access_token(self, refresh_token: str) -> dict:
        """Get a new access token from the refresh token."""
        response = requests.post(
            TOKEN_URL,
            data={"grant_type": "refresh_token", "refresh_token": refresh_token},
            auth=(settings.SPOTIFY_CLIENT_ID, settings.SPOTIFY_CLIENT_SECRET),
        )
        response.raise_for_status()
        return response.json()

    def get_current_user(self, access_token: str) -> dict:
        """Return the Spotify profile of the authenticated user."""
        response = requests.get(
            f"{API_URL}/me",
            headers={"Authorization": f"Bearer {access_token}"},
        )
        response.raise_for_status()
        return response.json()

    def create_playlist(self, access_token: str) -> str:
        """Create a private meloDrop playlist on Spotify and return its id."""
        response = requests.post(
            f"{API_URL}/me/playlists",
            json={
                "name": PLAYLIST_NAME,
                "description": "Titres découverts sur meloDrop",
                "public": False,
            },
            headers={"Authorization": f"Bearer {access_token}"},
        )
        response.raise_for_status()
        return response.json()["id"]