"""Abstract base class that every streaming provider must implement."""

from abc import ABC, abstractmethod


class StreamingProvider(ABC):
    """Common interface that every streaming platform provider must implement."""

    @abstractmethod
    def search(self, query: str) -> list[dict]:
        """Search tracks matching the query."""
        ...

    @abstractmethod
    def get_track(self, track_id: str) -> dict:
        """Return a track's details (ISRC, title, preview URL, ...)."""
        ...

    @abstractmethod
    def add_favorite(self, access_token: str, track_id: str, playlist_id: str) -> None:
        """Add a track to the user's meloDrop playlist on the platform."""
        ...

    @abstractmethod
    def exchange_code(self, code: str) -> dict:
        """Exchange an OAuth code for an access token (and related data)."""
        ...