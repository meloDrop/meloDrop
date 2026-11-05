"""Automatic badges: a rule and a threshold set from the dashboard, checked after each drop and swipe.

A badge given automatically is never taken back by itself: a lower like rate
or a raised threshold does not remove it. The admin can still take it back.
"""

import logging
from collections import defaultdict
from datetime import date, timedelta, timezone
from zoneinfo import ZoneInfo

from sqlalchemy import func
from sqlalchemy.orm import Session

from app.models.badge import Badge
from app.models.drop import Drop
from app.models.save import Save
from app.models.skip import Skip
from app.models.user_badge import UserBadge

logger = logging.getLogger("melodrop")

# Views needed before the like rate counts: 3 likes out of 3 views is not a real rate
MIN_VIEWS_FOR_RATE = 30
# The days of a streak are French days: a drop at 0:30 counts for the new day
PARIS = ZoneInfo("Europe/Paris")

# Rules to check after a drop (for its author), and after a swipe (for the author of the drop)
DROP_RULES = ("drops", "streak")
SWIPE_RULES = ("likes_received", "picks_received", "like_rate")


def longest_streak(days: set[date]) -> int:
    """The most days in a row in the set: {1, 2, 3, 5} gives 3."""
    best = 0
    run = 0
    previous = None
    for day in sorted(days):
        if previous is not None and day - previous == timedelta(days=1):
            run += 1
        else:
            run = 1
        best = max(best, run)
        previous = day
    return best


def count_by_author(db: Session, table, user_ids: list[int] | None, picks_only: bool = False) -> dict[int, int]:
    """Rows of saves or skips on the drops of each author: {author id: number}."""
    query = db.query(Drop.user_id, func.count(table.id)).join(table, table.drop_id == Drop.id)
    if picks_only:
        query = query.filter(Save.is_pick.is_(True))
    if user_ids is not None:
        query = query.filter(Drop.user_id.in_(user_ids))
    return dict(query.group_by(Drop.user_id).all())


def rule_values(db: Session, rule: str, user_ids: list[int] | None = None) -> dict[int, int]:
    """The number of each user for a rule: {user id: number}. user_ids None: every user."""
    if rule == "drops":
        query = db.query(Drop.user_id, func.count(Drop.id))
        if user_ids is not None:
            query = query.filter(Drop.user_id.in_(user_ids))
        return dict(query.group_by(Drop.user_id).all())

    if rule == "streak":
        query = db.query(Drop.user_id, Drop.created_at)
        if user_ids is not None:
            query = query.filter(Drop.user_id.in_(user_ids))
        # Dates are stored in UTC (ADR-0001): each drop counts for its day in France
        days = defaultdict(set)
        for user_id, created_at in query.all():
            days[user_id].add(created_at.replace(tzinfo=timezone.utc).astimezone(PARIS).date())
        return {user_id: longest_streak(user_days) for user_id, user_days in days.items()}

    if rule == "likes_received":
        return count_by_author(db, Save, user_ids)

    if rule == "picks_received":
        return count_by_author(db, Save, user_ids, picks_only=True)

    if rule == "like_rate":
        likes = count_by_author(db, Save, user_ids)
        skips = count_by_author(db, Skip, user_ids)
        rates = {}
        for user_id in set(likes) | set(skips):
            views = likes.get(user_id, 0) + skips.get(user_id, 0)
            # Too few views: no rate yet
            if views >= MIN_VIEWS_FOR_RATE:
                rates[user_id] = round(100 * likes.get(user_id, 0) / views)
        return rates

    # A rule removed from BadgeRule: nobody gets the badge any more
    return {}


def award_by_rule(db: Session, badge: Badge, user_ids: list[int] | None = None) -> list[int]:
    """Give the badge to the users who reach its threshold and do not have it yet.

    Returns their ids. Nothing is saved: the caller commits.
    """
    if badge.rule is None or badge.threshold is None:
        return []

    values = rule_values(db, badge.rule, user_ids)
    owners = {row.user_id for row in db.query(UserBadge.user_id).filter(UserBadge.badge_id == badge.id).all()}

    awarded = []
    for user_id, value in sorted(values.items()):
        if value >= badge.threshold and user_id not in owners:
            db.add(UserBadge(user_id=user_id, badge_id=badge.id))
            awarded.append(user_id)
    return awarded


def check_auto_badges(db: Session, user_id: int, rules: tuple[str, ...]) -> None:
    """After a drop or a swipe: give the user the automatic badges now reached.

    Called once the drop or the swipe is saved. A problem here is logged and
    must not turn the drop or the swipe into an error: it is already saved.
    """
    try:
        badges = db.query(Badge).filter(Badge.rule.in_(list(rules))).all()
        awarded = False
        for badge in badges:
            if award_by_rule(db, badge, [user_id]):
                awarded = True
        if awarded:
            db.commit()
    except Exception:
        db.rollback()
        logger.exception("Automatic badges not checked for user %s", user_id)
