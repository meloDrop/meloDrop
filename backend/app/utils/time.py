"""Date helpers."""

from datetime import datetime, timezone


def utc_now() -> datetime:
    """Return the current UTC time without timezone, like a MySQL DATETIME."""
    return datetime.now(timezone.utc).replace(tzinfo=None)

def iso(value: datetime | None) -> str | None:
    """Turn a UTC datetime from the database into ISO text ending with Z."""
    return value.strftime("%Y-%m-%dT%H:%M:%SZ") if value else None