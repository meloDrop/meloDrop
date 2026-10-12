"""Date helpers."""

from datetime import datetime, timezone


def utc_now() -> datetime:
    """Return the current UTC time without timezone, like a MySQL DATETIME."""
    return datetime.now(timezone.utc).replace(tzinfo=None)