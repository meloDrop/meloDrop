"""Numbers of the overview tab of the admin dashboard (read only)."""

from collections import Counter
from datetime import timedelta

from sqlalchemy import text
from sqlalchemy.orm import Session

from app.utils.time import iso, utc_now

# Length of the daily curves, in days
DAYS = 14
# Number of events in the activity list
ACTIVITY_SIZE = 40


def count(db: Session, sql: str, params: dict | None = None) -> int:
    """Run a SELECT COUNT(*) and return the number."""
    return db.execute(text(sql), params or {}).scalar()


def get_totals(db: Session, now) -> dict:
    """Totals since the start."""
    return {
        "users": count(db, "SELECT COUNT(*) FROM users"),
        "unverified": count(db, "SELECT COUNT(*) FROM users WHERE email_verified = 0"),
        "drops": count(db, "SELECT COUNT(*) FROM drops"),
        "online_drops": count(db, "SELECT COUNT(*) FROM drops WHERE expires_at > :now", {"now": now}),
        "saves": count(db, "SELECT COUNT(*) FROM saves"),
        "skips": count(db, "SELECT COUNT(*) FROM skips"),
        "liked_songs": count(db, "SELECT COUNT(*) FROM liked_songs"),
        "rescues": count(db, "SELECT COUNT(*) FROM liked_songs WHERE liked_late = 1"),
    }


def get_last_24h(db: Session, now) -> dict:
    """What happened in the last 24 hours."""
    params = {"since": now - timedelta(hours=24)}
    return {
        "signups": count(db, "SELECT COUNT(*) FROM users WHERE created_at >= :since", params),
        "drops": count(db, "SELECT COUNT(*) FROM drops WHERE created_at >= :since", params),
        "saves": count(db, "SELECT COUNT(*) FROM saves WHERE created_at >= :since", params),
        "skips": count(db, "SELECT COUNT(*) FROM skips WHERE created_at >= :since", params),
        # Songs rescued from "Mes skips" after their drop expired
        "rescues": count(db, """
            SELECT COUNT(*) FROM liked_songs WHERE liked_late = 1 AND created_at >= :since
        """, params),
        # Users who dropped or swiped at least once
        "active_users": count(db, """
            SELECT COUNT(DISTINCT user_id) FROM (
                SELECT user_id FROM drops WHERE created_at >= :since
                UNION ALL
                SELECT user_id FROM saves WHERE created_at >= :since
                UNION ALL
                SELECT user_id FROM skips WHERE created_at >= :since
            ) AS recent
        """, params),
    }


def get_hourly(db: Session, now) -> dict:
    """Count each table's new rows per hour, in UTC.

    The page turns the UTC hours into French time and adds them up per day.
    """
    # One day more than the curves, so the oldest French day is complete
    since = now - timedelta(days=DAYS + 1)
    hourly = {}
    for table in ("users", "drops", "saves", "skips"):
        rows = db.execute(
            text(f"SELECT created_at FROM {table} WHERE created_at >= :since"),
            {"since": since},
        )
        hours = Counter(row.created_at.strftime("%Y-%m-%dT%H:00:00Z") for row in rows)
        hourly[table] = dict(hours)
    return hourly


def get_activity(db: Session) -> list[dict]:
    """Last events of all users: drops, likes, skips, rescues and new accounts.

    A rescue on a drop still online becomes a normal save: it shows as a like.
    A rescue on an expired drop keeps its skip and adds a liked_songs row with
    liked_late = 1: it shows as a rescue, with the author of the skipped drop.
    """
    rows = db.execute(text("""
        SELECT * FROM (
            SELECT d.created_at AS at, 'drop' AS type, u.id AS user_id, u.username,
                   s.title, s.artist_name AS artist, NULL AS author_id, NULL AS author
            FROM drops AS d
            JOIN users AS u ON u.id = d.user_id
            JOIN songs AS s ON s.id = d.song_id

            UNION ALL
            SELECT sv.created_at, 'like', u.id, u.username, s.title, s.artist_name, a.id, a.username
            FROM saves AS sv
            JOIN users AS u ON u.id = sv.user_id
            JOIN drops AS d ON d.id = sv.drop_id
            JOIN songs AS s ON s.id = d.song_id
            JOIN users AS a ON a.id = d.user_id

            UNION ALL
            SELECT sk.created_at, 'skip', u.id, u.username, s.title, s.artist_name, a.id, a.username
            FROM skips AS sk
            JOIN users AS u ON u.id = sk.user_id
            JOIN drops AS d ON d.id = sk.drop_id
            JOIN songs AS s ON s.id = d.song_id
            JOIN users AS a ON a.id = d.user_id

            UNION ALL
            SELECT l.created_at, 'rescue', u.id, u.username, s.title, s.artist_name, a.id, a.username
            FROM liked_songs AS l
            JOIN users AS u ON u.id = l.user_id
            JOIN songs AS s ON s.id = l.song_id
            -- Author of the last drop of this song the user skipped
            LEFT JOIN users AS a ON a.id = (
                SELECT d.user_id FROM skips AS sk
                JOIN drops AS d ON d.id = sk.drop_id
                WHERE sk.user_id = l.user_id AND d.song_id = l.song_id
                ORDER BY sk.created_at DESC
                LIMIT 1
            )
            WHERE l.liked_late = 1

            UNION ALL
            SELECT u.created_at, 'signup', u.id, u.username, NULL, NULL, NULL, NULL
            FROM users AS u
        ) AS events
        ORDER BY at DESC
        LIMIT :size
    """), {"size": ACTIVITY_SIZE}).mappings()
    return [{**row, "at": iso(row["at"])} for row in rows]


def get_online_drops(db: Session, now) -> list[dict]:
    """Drops still online, the most liked first."""
    rows = db.execute(text("""
        SELECT d.id AS drop_id, u.id AS user_id, u.username, s.id AS song_id,
               s.title, s.artist_name AS artist, s.cover_url, d.expires_at,
               (SELECT COUNT(*) FROM saves AS sv WHERE sv.drop_id = d.id) AS saves,
               (SELECT COUNT(*) FROM skips AS sk WHERE sk.drop_id = d.id) AS skips
        FROM drops AS d
        JOIN users AS u ON u.id = d.user_id
        JOIN songs AS s ON s.id = d.song_id
        WHERE d.expires_at > :now
        ORDER BY saves DESC, d.expires_at
    """), {"now": now}).mappings()
    return [{**row, "expires_at": iso(row["expires_at"])} for row in rows]


def get_users(db: Session) -> list[dict]:
    """Each user with their counters, the most recently active first."""
    rows = db.execute(text("""
        SELECT u.id, u.username, u.display_name, u.avatar_color, u.avatar_style, u.email, u.email_verified, u.created_at,
               (SELECT COUNT(*) FROM drops AS d WHERE d.user_id = u.id)       AS drops,
               (SELECT COUNT(*) FROM saves AS sv WHERE sv.user_id = u.id)     AS saves,
               (SELECT COUNT(*) FROM skips AS sk WHERE sk.user_id = u.id)     AS skips,
               (SELECT COUNT(*)
                  FROM saves AS sv
                  JOIN drops AS d ON d.id = sv.drop_id
                 WHERE d.user_id = u.id)                                       AS saves_received,
               (SELECT COUNT(*) FROM liked_songs AS l WHERE l.user_id = u.id) AS liked_songs,
               (SELECT MAX(d.created_at) FROM drops AS d WHERE d.user_id = u.id)    AS last_drop,
               (SELECT MAX(sv.created_at) FROM saves AS sv WHERE sv.user_id = u.id) AS last_save,
               (SELECT MAX(sk.created_at) FROM skips AS sk WHERE sk.user_id = u.id) AS last_skip
        FROM users AS u
    """)).mappings()

    users = []
    for row in rows:
        # Last activity: the newest date among the account, its drops and swipes
        dates = [row["created_at"], row["last_drop"], row["last_save"], row["last_skip"]]
        last_activity = max(date for date in dates if date is not None)
        users.append({
            "id": row["id"],
            "username": row["username"],
            "display_name": row["display_name"],
            "avatar_color": row["avatar_color"],
            "avatar_style": row["avatar_style"],
            "email": row["email"],
            "verified": bool(row["email_verified"]),
            "created_at": iso(row["created_at"]),
            "drops": row["drops"],
            "saves": row["saves"],
            "skips": row["skips"],
            "saves_received": row["saves_received"],
            "liked_songs": row["liked_songs"],
            "last_activity": iso(last_activity),
            "last_drop": iso(row["last_drop"]),
        })
    users.sort(key=lambda user: user["last_activity"], reverse=True)
    return users


def get_overview(db: Session) -> dict:
    """Run every query of the overview and return one dictionary."""
    # Dates are stored in UTC without timezone (ADR-0001)
    now = utc_now()
    return {
        "generated_at": iso(now),
        "days": DAYS,
        "totals": get_totals(db, now),
        "last_24h": get_last_24h(db, now),
        "hourly": get_hourly(db, now),
        "activity": get_activity(db),
        "online_drops": get_online_drops(db, now),
        "users": get_users(db),
    }