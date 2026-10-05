"""Business logic for linking streaming accounts."""

from sqlalchemy.orm import Session

from app.config import settings
from app.exceptions import OAuthDeniedError
from app.models.platform_link import PlatformLink
from app.providers.deezer import DeezerProvider
from app.utils.security import create_state_token, decode_state_token

DEEZER_AUTH_URL = "https://connect.deezer.com/oauth/auth.php"
DEEZER_PERMS = "basic_access,manage_library,offline_access"

deezer_provider = DeezerProvider()


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

    link = (
        db.query(PlatformLink)
        .filter(PlatformLink.user_id == user_id, PlatformLink.provider == "deezer")
        .first()
    )
    if link is None:
        link = PlatformLink(user_id=user_id, provider="deezer")
        db.add(link)

    link.provider_user_id = str(profile["id"])
    link.access_token = access_token

    db.commit()
    db.refresh(link)
    return link