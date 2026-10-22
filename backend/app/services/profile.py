"""Functions that build a user's profile."""

from datetime import timezone

from sqlalchemy.orm import Session

from app.exceptions import UserNotFoundError
from app.models.drop import Drop
from app.models.save import Save
from app.models.song import Song
from app.models.user import User
from app.schemas.profile import ProfileDropOut, ProfileOut, ProfileUpdate
from app.services.drops import get_active_drop
from app.services.stats import get_stats
from app.utils.time import utc_now

# Number of past drops shown on a profile
RECENT_DROPS_SIZE = 5


def build_drop(db: Session, drop: Drop) -> ProfileDropOut:
    """Build a drop of the profile, with its song and its likes."""
    # Get the song of the drop
    song = db.get(Song, drop.song_id)

    # Count the likes of the drop
    saves_count = db.query(Save).filter(Save.drop_id == drop.id).count()

    return ProfileDropOut(
        id=drop.id,
        song_id=song.id,
        title=song.title,
        artist=song.artist_name,
        cover_url=song.cover_url,
        genre=song.genre,
        dropped_at=drop.created_at.replace(tzinfo=timezone.utc),
        saves_count=saves_count,
    )


def get_profile(db: Session, user_id: int) -> ProfileOut:
    """Return a user's profile: account, counters, drop of the day and last drops."""
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
        stats=get_stats(db, user.id),
        current_drop=current_drop,
        recent_drops=recent_drops,
    )



def update_profile(db: Session, user: User, changes: ProfileUpdate) -> ProfileOut:
    """Change the editable part of the user's own profile, then return the profile."""
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

    db.commit()
    return get_profile(db, user.id)