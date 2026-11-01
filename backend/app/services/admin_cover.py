"""Deezer covers sent through the API, for the shared image of the Récap tab.

The page draws the Récap card as an image, and a browser refuses to draw
images that come from another site: the covers must come from the API.
"""

from urllib.parse import urlsplit

import requests

from app.exceptions import CoverNotAllowedError, CoverNotFoundError

# Only the Deezer image servers
COVER_HOST = ".dzcdn.net"
# Biggest cover accepted, in bytes
COVER_MAX_BYTES = 2_000_000
# Seconds to wait for Deezer
TIMEOUT = 10
# Covers kept in memory before the cache is emptied
CACHE_SIZE = 300

# Covers already downloaded: url -> (type, bytes)
cover_cache: dict[str, tuple[str, bytes]] = {}


def is_cover_url(url: str) -> bool:
    """True for an https link to the Deezer image servers."""
    parts = urlsplit(url)
    return parts.scheme == "https" and (parts.hostname or "").endswith(COVER_HOST)


def get_cover(url: str) -> tuple[str, bytes]:
    """Download a Deezer cover once, then answer from the cache."""
    if not is_cover_url(url):
        raise CoverNotAllowedError("cover_not_allowed")
    if url in cover_cache:
        return cover_cache[url]

    try:
        # No redirect: the request must stay on the Deezer server it was sent to
        response = requests.get(url, timeout=TIMEOUT, allow_redirects=False, stream=True)
        response.raise_for_status()
    except requests.RequestException:
        raise CoverNotFoundError("cover_not_found")

    content_type = response.headers.get("Content-Type", "").split(";")[0]
    if response.status_code != 200 or not content_type.startswith("image/"):
        raise CoverNotFoundError("cover_not_found")

    # Read the image piece by piece, and stop if it is too big
    body = b""
    for chunk in response.iter_content(64 * 1024):
        body += chunk
        if len(body) > COVER_MAX_BYTES:
            raise CoverNotFoundError("cover_not_found")

    if len(cover_cache) >= CACHE_SIZE:
        cover_cache.clear()
    cover_cache[url] = (content_type, body)
    return cover_cache[url]