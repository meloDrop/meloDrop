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

