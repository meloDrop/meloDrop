"""Business logic for linking streaming accounts."""

from datetime import datetime, timedelta, timezone
from urllib.parse import quote

from sqlalchemy.orm import Session

from app.config import settings
from app.exceptions import OAuthDeniedError, AccountAlreadyLinkedError
from app.models.platform_link import PlatformLink
from app.providers.deezer import DeezerProvider
from app.providers.spotify import SpotifyProvider
from app.utils.security import create_state_token, decode_state_token

DEEZER_AUTH_URL = "https://connect.deezer.com/oauth/auth.php"
DEEZER_PERMS = "basic_access,manage_library,offline_access"

SPOTIFY_AUTH_URL = "https://accounts.spotify.com/authorize"
SPOTIFY_SCOPES = "playlist-modify-public playlist-modify-private"

deezer_provider = DeezerProvider()
spotify_provider = SpotifyProvider()


def find_or_create_link(db: Session, user_id: int, provider: str) -> PlatformLink:
    """Return the user's link for this platform, or a new one."""
    link = (
        db.query(PlatformLink)
        .filter(PlatformLink.user_id == user_id, PlatformLink.provider == provider)
        .first()
    )
    if link is None:
        link = PlatformLink(user_id=user_id, provider=provider)
        db.add(link)
    return link

def check_account_is_free(db: Session, user_id: int, provider: str, provider_user_id: str) -> None:
    """Raise an error if this streaming account is already linked to another user."""
    already_linked = (
        db.query(PlatformLink)
        .filter(
            PlatformLink.provider == provider,
            PlatformLink.provider_user_id == provider_user_id,
            PlatformLink.user_id != user_id,
        )
        .first()
    )
    if already_linked is not None:
        raise AccountAlreadyLinkedError()

def start_deezer_link(user_id: int) -> str:
    """Build the Deezer authorization URL containing a signed state."""
    state = create_state_token(user_id)
    return (
        f"{DEEZER_AUTH_URL}?app_id={settings.DEEZER_APP_ID}"
        f"&redirect_uri={settings.DEEZER_REDIRECT_URI}"
        f"&perms={DEEZER_PERMS}&state={state}"
    )


def handle_deezer_callback(
    db: Session, code: str | None, state: str, error_reason: str | None
) -> PlatformLink:
    """Finish the Deezer OAuth flow: verify state, exchange code, store the link."""
    user_id = decode_state_token(state)

    if error_reason == "user_denied":
        raise OAuthDeniedError()

    token_data = deezer_provider.exchange_code(code)
    access_token = token_data["access_token"]
    profile = deezer_provider.get_current_user(access_token)
    check_account_is_free(db, user_id, "deezer", str(profile["id"]))

    link = find_or_create_link(db, user_id, "deezer")
    link.provider_user_id = str(profile["id"])
    link.access_token = access_token

    db.commit()
    db.refresh(link)
    return link


def start_spotify_link(user_id: int) -> str:
    """Build the Spotify authorization URL containing a signed state."""
    state = create_state_token(user_id)
    redirect_uri = quote(settings.SPOTIFY_REDIRECT_URI, safe="")
    scope = quote(SPOTIFY_SCOPES)
    return (
        f"{SPOTIFY_AUTH_URL}?client_id={settings.SPOTIFY_CLIENT_ID}"
        f"&response_type=code&redirect_uri={redirect_uri}"
        f"&scope={scope}&state={state}"
    )


def handle_spotify_callback(
    db: Session, code: str | None, state: str, error: str | None
) -> PlatformLink:
    """Finish the Spotify OAuth flow: store the tokens and create the playlist once."""
    user_id = decode_state_token(state)

    if error is not None:
        raise OAuthDeniedError()

    token_data = spotify_provider.exchange_code(code)
    access_token = token_data["access_token"]
    profile = spotify_provider.get_current_user(access_token)
    check_account_is_free(db, user_id, "spotify", profile["id"])

    link = find_or_create_link(db, user_id, "spotify")
    link.provider_user_id = profile["id"]
    link.access_token = access_token
    link.refresh_token = token_data["refresh_token"]
    # stored in UTC without timezone, like MySQL DATETIME
    now = datetime.now(timezone.utc).replace(tzinfo=None)
    link.expires_at = now + timedelta(seconds=token_data["expires_in"])

    if link.spotify_playlist_id is None:
        link.spotify_playlist_id = spotify_provider.create_playlist(access_token)

    db.commit()
    db.refresh(link)
    return link