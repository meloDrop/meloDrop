"""Everything about one user, for the Utilisateurs tab of the admin dashboard (read only)."""

from collections import Counter
from datetime import datetime, timedelta

from sqlalchemy import text
from sqlalchemy.orm import Session

from app.exceptions import UserNotFoundError
from app.models.report import Report
from app.models.user import User
from app.services.feed import SKIPPED_DAYS
from app.services.likes import get_likes
from app.services.profile import get_profile
from app.services.reports import build_report
from app.services.skips import get_skips
from app.utils.time import iso, utc_now

# Length of the activity curve, in days
DAYS = 14
# Most recent lines kept in each history (likes, skips, drops, follows)
HISTORY_SIZE = 100

# Rows of the user counted per hour for the curve: name, table and filter
HOURLY_SOURCES = {
    "drops": ("drops", ""),
    "saves": ("saves", ""),
    "likes": ("saves", "AND is_pick = 0"),
    "picks": ("saves", "AND is_pick = 1"),
    "skips": ("skips", ""),
}

# A skip rescued after its drop expired: the skip stays, and the song is
# in "Mes likes" with liked_late = 1 (a rescue on a drop online becomes a save)
RESCUED = """EXISTS (SELECT 1 FROM liked_songs AS l
                 WHERE l.user_id = :id AND l.song_id = d.song_id AND l.liked_late = 1)"""


def fetch(db: Session, sql: str, params: dict) -> list[dict]:
    """Run a SELECT and return its rows as dictionaries, dates in ISO."""
    rows = db.execute(text(sql), params).mappings()
    return [
        {key: iso(value) if isinstance(value, datetime) else value for key, value in row.items()}
        for row in rows
    ]


def count(db: Session, sql: str, params: dict) -> int:
    """Run a SELECT COUNT(*) and return the number."""
    return db.execute(text(sql), params).scalar()


def get_account(db: Session, user: User) -> dict:
    """Private data of the account, never sent to the app."""
    codes = fetch(db, """
        SELECT purpose, attempts, expires_at, created_at
        FROM email_codes
        WHERE user_id = :id
        ORDER BY created_at DESC
    """, {"id": user.id})
    return {
        "id": user.id,
        "username": user.username,
        "display_name": user.display_name,
        "email": user.email,
        "email_verified": bool(user.email_verified),
        "pending_email": user.pending_email,
        "avatar_color": user.avatar_color,
        "avatar_style": user.avatar_style,
        "avatar_drop": user.avatar_drop,
        "avatar_deco": user.avatar_deco,
        "avatar_face": user.avatar_face,
        "avatar_aura": user.avatar_aura,
        "is_subscriber": bool(user.is_subscriber),
        "role": user.role,
        "created_at": iso(user.created_at),
        "updated_at": iso(user.updated_at),
        # Codes waiting to be typed (verification, password, email change)
        "email_codes": codes,
    }


def get_stats(db: Session, user_id: int) -> dict:
    """Counters given and received by the user."""
    params = {"id": user_id}
    return {
        "drops": count(db, "SELECT COUNT(*) FROM drops WHERE user_id = :id", params),
        # saves: likes and picks given. picks: the up swipes only
        "saves": count(db, "SELECT COUNT(*) FROM saves WHERE user_id = :id", params),
        "picks": count(db, "SELECT COUNT(*) FROM saves WHERE user_id = :id AND is_pick = 1", params),
        "skips": count(db, "SELECT COUNT(*) FROM skips WHERE user_id = :id", params),
        "saves_received": count(db, """
            SELECT COUNT(*) FROM saves AS sv
            JOIN drops AS d ON d.id = sv.drop_id
            WHERE d.user_id = :id
        """, params),
        "picks_received": count(db, """
            SELECT COUNT(*) FROM saves AS sv
            JOIN drops AS d ON d.id = sv.drop_id
            WHERE d.user_id = :id AND sv.is_pick = 1
        """, params),
        "skips_received": count(db, """
            SELECT COUNT(*) FROM skips AS sk
            JOIN drops AS d ON d.id = sk.drop_id
            WHERE d.user_id = :id
        """, params),
        "liked_songs": count(db, "SELECT COUNT(*) FROM liked_songs WHERE user_id = :id", params),
        # Songs marked as picks in "Mes likes" (the "Mes picks" list)
        "picked_songs": count(db, "SELECT COUNT(*) FROM liked_songs WHERE user_id = :id AND is_pick = 1", params),
        "followers": count(db, "SELECT COUNT(*) FROM follows WHERE followed_id = :id", params),
        "following": count(db, "SELECT COUNT(*) FROM follows WHERE follower_id = :id", params),
        # Songs rescued from "Mes skips" after their drop expired
        "rescues": count(db, """
            SELECT COUNT(*) FROM liked_songs WHERE user_id = :id AND liked_late = 1
        """, params),
    }


def get_hourly(db: Session, user_id: int, now: datetime) -> dict:
    """The user's drops, likes, picks and skips per hour, in UTC."""
    since = now - timedelta(days=DAYS + 1)
    hourly = {}
    for name, (table, condition) in HOURLY_SOURCES.items():
        rows = db.execute(
            text(f"SELECT created_at FROM {table} WHERE user_id = :id AND created_at >= :since {condition}"),
            {"id": user_id, "since": since},
        )
        hourly[name] = dict(Counter(row.created_at.strftime("%Y-%m-%dT%H:00:00Z") for row in rows))
    return hourly


def get_current_drop_swipes(db: Session, drop_id: int) -> list[dict]:
    """Who liked, picked or skipped the drop in progress, the most recent first."""
    return fetch(db, """
        SELECT * FROM (
            SELECT sv.created_at AS at, CASE WHEN sv.is_pick = 1 THEN 'pick' ELSE 'like' END AS type,
                   u.id AS user_id, u.username
            FROM saves AS sv JOIN users AS u ON u.id = sv.user_id
            WHERE sv.drop_id = :drop_id
            UNION ALL
            SELECT sk.created_at, 'skip', u.id, u.username
            FROM skips AS sk JOIN users AS u ON u.id = sk.user_id
            WHERE sk.drop_id = :drop_id
        ) AS swipes
        ORDER BY at DESC
    """, {"drop_id": drop_id})


def get_feed(db: Session, user_id: int, now: datetime) -> list[dict]:
    """Every drop Discover can still show the user, one row per drop.

    Same filters as get_feed in services/feed.py, without the random pick of 10.
    The app shows the drops of the same song on one card (ADR-0021).
    """
    return fetch(db, """
        SELECT d.id AS drop_id, s.id AS song_id, s.title, s.artist_name AS artist, s.cover_url, s.genre,
               a.id AS author_id, a.username AS author_username, d.expires_at,
               (SELECT COUNT(*) FROM saves AS sv WHERE sv.drop_id = d.id) AS saves
        FROM drops AS d
        JOIN songs AS s ON s.id = d.song_id
        JOIN users AS a ON a.id = d.user_id
        WHERE d.expires_at > :now
          AND d.user_id <> :id
          AND d.id NOT IN (SELECT drop_id FROM saves WHERE user_id = :id)
          AND d.id NOT IN (SELECT drop_id FROM skips WHERE user_id = :id)
          -- Songs hidden: liked on a drop still online, skipped lately, or dropped by the user and still online
          AND d.song_id NOT IN (
              SELECT d3.song_id FROM saves AS sv3 JOIN drops AS d3 ON d3.id = sv3.drop_id
              WHERE sv3.user_id = :id AND d3.expires_at > :now
          )
          AND d.song_id NOT IN (
              SELECT d2.song_id FROM skips AS sk2 JOIN drops AS d2 ON d2.id = sk2.drop_id
              WHERE sk2.user_id = :id AND sk2.created_at >= :skipped_since
          )
          AND d.song_id NOT IN (SELECT song_id FROM drops WHERE user_id = :id AND expires_at > :now)
        ORDER BY d.expires_at
    """, {"id": user_id, "now": now, "skipped_since": now - timedelta(days=SKIPPED_DAYS)})


def get_swipes(db: Session, user_id: int, table: str) -> list[dict]:
    """Drops liked (saves) or skipped (skips) by the user, the most recent first."""
    if table == "saves":
        # For a like: was it a pick, and is the song still in "Mes likes"? (it can be removed, ADR-0013)
        extra = """, x.is_pick,
               (SELECT COUNT(*) FROM liked_songs AS l
                 WHERE l.user_id = :id AND l.song_id = d.song_id) AS in_likes"""
    else:
        # For a skip: was the song rescued later from "Mes skips"?
        extra = f""",
               {RESCUED} AS rescued"""
    return fetch(db, f"""
        SELECT x.created_at AS at, d.id AS drop_id, s.id AS song_id, s.title, s.artist_name AS artist, s.cover_url,
               a.id AS author_id, a.username AS author_username{extra}
        FROM {table} AS x
        JOIN drops AS d ON d.id = x.drop_id
        JOIN songs AS s ON s.id = d.song_id
        JOIN users AS a ON a.id = d.user_id
        WHERE x.user_id = :id
        ORDER BY x.created_at DESC
        LIMIT :size
    """, {"id": user_id, "size": HISTORY_SIZE})


def get_drops(db: Session, user_id: int) -> list[dict]:
    """All the drops of the user, with their likes, picks and skips."""
    return fetch(db, """
        SELECT d.id AS drop_id, s.id AS song_id, s.title, s.artist_name AS artist, s.cover_url, s.genre,
               d.created_at, d.expires_at,
               (SELECT COUNT(*) FROM saves AS sv WHERE sv.drop_id = d.id) AS saves,
               (SELECT COUNT(*) FROM saves AS sv WHERE sv.drop_id = d.id AND sv.is_pick = 1) AS picks,
               (SELECT COUNT(*) FROM skips AS sk WHERE sk.drop_id = d.id) AS skips
        FROM drops AS d
        JOIN songs AS s ON s.id = d.song_id
        WHERE d.user_id = :id
        ORDER BY d.created_at DESC
        LIMIT :size
    """, {"id": user_id, "size": HISTORY_SIZE})


def get_affinities(db: Session, user_id: int) -> dict:
    """Who likes the user's drops the most, and whose drops the user likes the most."""
    params = {"id": user_id}
    fans = fetch(db, """
        SELECT u.id AS user_id, u.username, COUNT(*) AS likes
        FROM saves AS sv
        JOIN drops AS d ON d.id = sv.drop_id
        JOIN users AS u ON u.id = sv.user_id
        WHERE d.user_id = :id
        GROUP BY u.id, u.username
        ORDER BY likes DESC, u.username
        LIMIT 5
    """, params)
    favorites = fetch(db, """
        SELECT a.id AS user_id, a.username, COUNT(*) AS likes
        FROM saves AS sv
        JOIN drops AS d ON d.id = sv.drop_id
        JOIN users AS a ON a.id = d.user_id
        WHERE sv.user_id = :id
        GROUP BY a.id, a.username
        ORDER BY likes DESC, a.username
        LIMIT 5
    """, params)
    return {"fans": fans, "favorites": favorites}


def get_follows(db: Session, user_id: int) -> dict:
    """Who follows the user, and who the user follows, the most recent first."""
    params = {"id": user_id, "size": HISTORY_SIZE}
    followers = fetch(db, """
        SELECT f.created_at AS at, u.id AS user_id, u.username, u.avatar_color, u.avatar_style,
               -- The user follows them back
               EXISTS (SELECT 1 FROM follows AS b
                        WHERE b.follower_id = :id AND b.followed_id = u.id) AS mutual
        FROM follows AS f
        JOIN users AS u ON u.id = f.follower_id
        WHERE f.followed_id = :id
        ORDER BY f.created_at DESC, f.id DESC
        LIMIT :size
    """, params)
    following = fetch(db, """
        SELECT f.created_at AS at, u.id AS user_id, u.username, u.avatar_color, u.avatar_style,
               -- They follow the user back
               EXISTS (SELECT 1 FROM follows AS b
                        WHERE b.follower_id = u.id AND b.followed_id = :id) AS mutual
        FROM follows AS f
        JOIN users AS u ON u.id = f.followed_id
        WHERE f.follower_id = :id
        ORDER BY f.created_at DESC, f.id DESC
        LIMIT :size
    """, params)
    for row in followers + following:
        row["mutual"] = bool(row["mutual"])
    return {"followers": followers, "following": following}


def get_reports(db: Session, user_id: int) -> dict:
    """Reports about the user, and reports sent by the user, the newest first."""
    received = (
        db.query(Report)
        .filter(Report.reported_id == user_id)
        .order_by(Report.created_at.desc(), Report.id.desc())
        .all()
    )
    sent = db.query(Report).filter(Report.reporter_id == user_id).count()
    return {
        "received": [build_report(db, report).model_dump(mode="json") for report in received],
        "sent": sent,
    }


def get_user_detail(db: Session, user_id: int) -> dict:
    """Run every query for one user and return one dictionary, or raise."""
    user = db.get(User, user_id)
    if user is None:
        raise UserNotFoundError("user_not_found")

    # Dates are stored in UTC without timezone (ADR-0001)
    now = utc_now()
    profile = get_profile(db, user_id)
    current = profile.current_drop

    return {
        "generated_at": iso(now),
        "days": DAYS,
        "account": get_account(db, user),
        # Same data as GET /users/{id}/profile in the app
        "profile": profile.model_dump(mode="json"),
        "stats": get_stats(db, user_id),
        "hourly": get_hourly(db, user_id, now),
        "current_drop_swipes": get_current_drop_swipes(db, current.id) if current else [],
        "feed": get_feed(db, user_id, now),
        # Same data as GET /me/likes in the app
        "likes": [like.model_dump(mode="json") for like in get_likes(db, user_id)],
        # Same data as GET /me/skips in the app: the last 7 days, not rescued
        "my_skips": [skip.model_dump(mode="json") for skip in get_skips(db, user_id)],
        "saves": get_swipes(db, user_id, "saves"),
        "skips": get_swipes(db, user_id, "skips"),
        "drops": get_drops(db, user_id),
        "affinities": get_affinities(db, user_id),
        "follows": get_follows(db, user_id),
        "reports": get_reports(db, user_id),
    }