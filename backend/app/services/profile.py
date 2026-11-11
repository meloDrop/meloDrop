"""Functions that build a user's profile."""

from datetime import timezone

from sqlalchemy.orm import Session

from app.config import settings
from app.exceptions import PremiumRequiredError, UserNotFoundError
from app.models.drop import Drop
from app.models.song import Song
from app.models.user import User
from app.schemas.profile import ProfileDropOut, ProfileOut, ProfileUpdate
from app.services.drops import count_swipes, get_active_drop
from app.services.stats import get_stats
from app.utils.time import utc_now
from app.services.badges import get_user_badges
from app.services.follows import count_followers, count_following, is_following
from app.services.labeled_songs import get_labeled_songs
from app.services.podium import get_podium

# Number of past drops shown on a profile
RECENT_DROPS_SIZE = 5

# Drop colors that need the subscription once PREMIUM_LOCKED is true
PREMIUM_DROPS = {"ocean", "plum", "gold", "spectre"}


def build_drop(db: Session, drop: Drop) -> ProfileDropOut:
    """Build a drop of the profile, with its song, its likes, picks and views."""
    # Get the song of the drop
    song = db.get(Song, drop.song_id)

    return ProfileDropOut(
        id=drop.id,
        song_id=song.id,
        title=song.title,
        artist=song.artist_name,
        cover_url=song.cover_url,
        genre=song.genre,
        dropped_at=drop.created_at.replace(tzinfo=timezone.utc),
        **count_swipes(db, drop.id),
    )


def get_profile(db: Session, user_id: int, viewer_id: int | None = None) -> ProfileOut:
    """Return a user's profile: account, counters, drop of the day and last drops.

    viewer_id: the logged-in user who looks at the profile, for the follow fields.
    None (the dashboard): nobody follows, nobody is followed.
    """
    # The user must exist
    user = db.get(User, user_id)
    if user is None:
        raise UserNotFoundError("user_not_found")

    # The drop online today, or None (same function as GET /drops/me)
    current = get_active_drop(db, user_id)
    current_drop = None
    if current is not None:
        current_drop = build_drop(db, current)

    # The last expired drops, the most recent first
    past = (
        db.query(Drop)
        .filter(Drop.user_id == user_id, Drop.expires_at <= utc_now())
        .order_by(Drop.expires_at.desc())
        .limit(RECENT_DROPS_SIZE)
        .all()
    )
    recent_drops = []
    for drop in past:
        recent_drops.append(build_drop(db, drop))

    return ProfileOut(
        id=user.id,
        username=user.username,
        display_name=user.display_name,
        created_at=user.created_at.replace(tzinfo=timezone.utc),
        avatar_color=user.avatar_color,
        avatar_style=user.avatar_style,
        avatar_drop=user.avatar_drop,
        avatar_deco=user.avatar_deco,
        avatar_face=user.avatar_face,
        avatar_aura=user.avatar_aura,
        stats=get_stats(db, user.id),
        current_drop=current_drop,
        recent_drops=recent_drops,
        podium=get_podium(db, user.id),
        labeled_songs=get_labeled_songs(db, user.id),
        badges=get_user_badges(db, user.id),
        followers_count=count_followers(db, user.id),
        following_count=count_following(db, user.id),
        is_followed=viewer_id is not None and is_following(db, viewer_id, user.id),
        follows_you=viewer_id is not None and is_following(db, user.id, viewer_id),
        is_me=viewer_id == user.id,
    )


def get_drops(db: Session, user_id: int) -> list[ProfileDropOut]:
    """Return all the drops of a user, the drop of the day included, the most recent first."""
    # The user must exist
    user = db.get(User, user_id)
    if user is None:
        raise UserNotFoundError("user_not_found")

    # No limit: every drop the user ever shared
    drops = (
        db.query(Drop)
        .filter(Drop.user_id == user_id)
        .order_by(Drop.created_at.desc())
        .all()
    )
    all_drops = []
    for drop in drops:
        all_drops.append(build_drop(db, drop))

    return all_drops


def premium_unlocked(user: User) -> bool:
    """True when the user can pick the premium cosmetics.

    Everything is free while PREMIUM_LOCKED is false (test phase),
    then only the subscribers get the premium drops and the auras.
    """
    return not settings.PREMIUM_LOCKED or user.is_subscriber


def check_premium(user: User, changes: ProfileUpdate) -> None:
    """Refuse a new premium drop or aura when the user cannot pick them.

    A premium choice the user already has is kept: a tester who chose it
    before the lock can still save the rest of the profile.
    """
    if premium_unlocked(user):
        return

    new_drop = changes.avatar_drop
    if new_drop in PREMIUM_DROPS and new_drop != user.avatar_drop:
        raise PremiumRequiredError("premium_required")

    new_aura = changes.avatar_aura
    if new_aura is not None and new_aura != "none" and new_aura != user.avatar_aura:
        raise PremiumRequiredError("premium_required")


def update_profile(db: Session, user: User, changes: ProfileUpdate, check_lock: bool = True) -> ProfileOut:
    """Change the editable part of the user's own profile, then return the profile.

    check_lock: False for the dashboard, where the admin can set any cosmetic.
    """
    if check_lock:
        check_premium(user, changes)

    # Only the fields sent are changed: None means "not sent"
    if changes.display_name is not None:
        # Spaces around the name are removed; an empty name gives back the username
        name = changes.display_name.strip()
        if name:
            user.display_name = name
        else:
            user.display_name = None

    # The schema already checked that the color and the style are in their lists
    if changes.avatar_color is not None:
        user.avatar_color = changes.avatar_color

    if changes.avatar_style is not None:
        user.avatar_style = changes.avatar_style

    # The schema checked these lists too
    if changes.avatar_drop is not None:
        user.avatar_drop = changes.avatar_drop

    if changes.avatar_deco is not None:
        user.avatar_deco = changes.avatar_deco

    if changes.avatar_face is not None:
        user.avatar_face = changes.avatar_face

    if changes.avatar_aura is not None:
        user.avatar_aura = changes.avatar_aura

    db.commit()
    return get_profile(db, user.id, user.id)