"""Functions that create the badges and give them to the users."""

from datetime import timezone

from sqlalchemy import func
from sqlalchemy.orm import Session

from app.exceptions import (
    BadgeAlreadyAwardedError,
    BadgeNameTakenError,
    BadgeNotFoundError,
    UserBadgeNotFoundError,
    UserNotFoundError,
)
from app.models.badge import Badge
from app.models.user import User
from app.models.user_badge import UserBadge
from app.schemas.badges import (
    BadgeAwardOut,
    BadgeCreate,
    BadgeHolderOut,
    BadgeOut,
    BadgeUpdate,
    UserBadgeOut,
)


def find_badge(db: Session, badge_id: int) -> Badge:
    """Return the badge with this id, or raise."""
    badge = db.get(Badge, badge_id)
    if badge is None:
        raise BadgeNotFoundError("badge_not_found")
    return badge


def find_user(db: Session, user_id: int) -> User:
    """Return the user with this id, or raise."""
    user = db.get(User, user_id)
    if user is None:
        raise UserNotFoundError("user_not_found")
    return user


def check_name_free(db: Session, name: str, badge_id: int | None = None) -> None:
    """Raise if another badge already has this name. Upper and lower case count as the same."""
    query = db.query(Badge).filter(func.lower(Badge.name) == name.lower())
    # When a badge is renamed, its own name does not count
    if badge_id is not None:
        query = query.filter(Badge.id != badge_id)
    if query.first() is not None:
        raise BadgeNameTakenError("badge_name_taken")


def build_badge(db: Session, badge: Badge) -> BadgeOut:
    """Build a badge for the dashboard, with the users who have it."""
    rows = (
        db.query(UserBadge, User)
        .join(User, User.id == UserBadge.user_id)
        .filter(UserBadge.badge_id == badge.id)
        .order_by(UserBadge.awarded_at.desc(), UserBadge.id.desc())
        .all()
    )

    holders = []
    for user_badge, user in rows:
        holders.append(
            BadgeHolderOut(
                user_id=user.id,
                username=user.username,
                awarded_at=user_badge.awarded_at.replace(tzinfo=timezone.utc),
            )
        )

    return BadgeOut(
        id=badge.id,
        name=badge.name,
        description=badge.description,
        icon=badge.icon,
        color=badge.color,
        created_at=badge.created_at.replace(tzinfo=timezone.utc),
        holders=holders,
    )


def list_badges(db: Session) -> list[BadgeOut]:
    """Return every badge, the newest first."""
    badges = db.query(Badge).order_by(Badge.created_at.desc(), Badge.id.desc()).all()

    all_badges = []
    for badge in badges:
        all_badges.append(build_badge(db, badge))
    return all_badges


def create_badge(db: Session, data: BadgeCreate) -> BadgeOut:
    """Create a badge. Nobody has it yet."""
    check_name_free(db, data.name)

    badge = Badge(name=data.name, description=data.description, icon=data.icon, color=data.color)
    db.add(badge)
    db.commit()
    db.refresh(badge)
    return build_badge(db, badge)


def update_badge(db: Session, badge_id: int, changes: BadgeUpdate) -> BadgeOut:
    """Change a badge. The users who have it see the change on their Mixtape."""
    badge = find_badge(db, badge_id)

    # Only the fields sent are changed: None means "not sent"
    if changes.name is not None:
        check_name_free(db, changes.name, badge.id)
        badge.name = changes.name

    if changes.description is not None:
        badge.description = changes.description

    # The schema already checked that the icon and the color are in their lists
    if changes.icon is not None:
        badge.icon = changes.icon

    if changes.color is not None:
        badge.color = changes.color

    db.commit()
    return build_badge(db, badge)


def delete_badge(db: Session, badge_id: int) -> None:
    """Delete a badge: it disappears from every profile."""
    badge = find_badge(db, badge_id)

    # MySQL also does it (ON DELETE CASCADE): done here so it does not depend on it
    db.query(UserBadge).filter(UserBadge.badge_id == badge.id).delete()
    db.delete(badge)
    db.commit()


def get_user_badges(db: Session, user_id: int) -> list[UserBadgeOut]:
    """Return the badges of a user, the most recently given first."""
    rows = (
        db.query(UserBadge, Badge)
        .join(Badge, Badge.id == UserBadge.badge_id)
        .filter(UserBadge.user_id == user_id)
        .order_by(UserBadge.awarded_at.desc(), UserBadge.id.desc())
        .all()
    )

    badges = []
    for user_badge, badge in rows:
        badges.append(
            UserBadgeOut(
                id=badge.id,
                name=badge.name,
                description=badge.description,
                icon=badge.icon,
                color=badge.color,
                awarded_at=user_badge.awarded_at.replace(tzinfo=timezone.utc),
            )
        )
    return badges


def list_user_badges(db: Session, user_id: int) -> list[UserBadgeOut]:
    """Return the badges of a user, after checking that the user exists."""
    find_user(db, user_id)
    return get_user_badges(db, user_id)


def award_badge(db: Session, user_id: int, badge_id: int) -> list[UserBadgeOut]:
    """Give a badge to a user, and return all their badges."""
    find_user(db, user_id)
    find_badge(db, badge_id)

    already = (
        db.query(UserBadge)
        .filter(UserBadge.user_id == user_id, UserBadge.badge_id == badge_id)
        .first()
    )
    if already is not None:
        raise BadgeAlreadyAwardedError("badge_already_awarded")

    db.add(UserBadge(user_id=user_id, badge_id=badge_id))
    db.commit()
    return get_user_badges(db, user_id)


def award_badge_many(db: Session, badge_id: int, user_ids: list[int]) -> BadgeAwardOut:
    """Give a badge to several users at once. Those who already have it are skipped."""
    badge = find_badge(db, badge_id)

    # The same id twice counts once, in the order of the list
    wanted = list(dict.fromkeys(user_ids))

    # Every user must exist: if one is missing, nobody gets the badge
    found = {user.id for user in db.query(User).filter(User.id.in_(wanted)).all()}
    if len(found) != len(wanted):
        raise UserNotFoundError("user_not_found")

    # The users who already have the badge
    rows = (
        db.query(UserBadge.user_id)
        .filter(UserBadge.badge_id == badge.id, UserBadge.user_id.in_(wanted))
        .all()
    )
    owners = {row.user_id for row in rows}

    awarded = []
    already_had = []
    for user_id in wanted:
        if user_id in owners:
            already_had.append(user_id)
        else:
            db.add(UserBadge(user_id=user_id, badge_id=badge.id))
            awarded.append(user_id)

    # All the new badges are saved together
    db.commit()
    return BadgeAwardOut(awarded=awarded, already_had=already_had, badge=build_badge(db, badge))


def revoke_badge(db: Session, user_id: int, badge_id: int) -> None:
    """Take a badge back from a user. The badge stays for the others."""
    find_user(db, user_id)

    row = (
        db.query(UserBadge)
        .filter(UserBadge.user_id == user_id, UserBadge.badge_id == badge_id)
        .first()
    )
    if row is None:
        raise UserBadgeNotFoundError("user_badge_not_found")

    db.delete(row)
    db.commit()
