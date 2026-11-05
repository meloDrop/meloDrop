"""All the drops, and who swiped one drop, for the Drops tab of the admin dashboard (read only)."""

from datetime import timedelta

from sqlalchemy import text
from sqlalchemy.orm import Session

from app.utils.time import iso, utc_now


def get_drops(db: Session) -> dict:
    """Every drop, the newest first, with its song, its author and its swipes."""
    # Dates are stored in UTC without timezone (ADR-0001)
    now = utc_now()
    rows = db.execute(text("""
        SELECT d.id AS drop_id, s.id AS song_id, s.title, s.artist_name AS artist,
               s.cover_url, s.genre, u.id AS author_id, u.username AS author_username,
               d.created_at, d.expires_at,
               -- saves: likes and picks. Views (saves + skips) are counted by the page
               (SELECT COUNT(*) FROM saves AS sv WHERE sv.drop_id = d.id) AS saves,
               (SELECT COUNT(*) FROM saves AS sv WHERE sv.drop_id = d.id AND sv.is_pick = 1) AS picks,
               (SELECT COUNT(*) FROM skips AS sk WHERE sk.drop_id = d.id) AS skips,
               -- Likes and picks received in the last 24 hours, the drop online or not
               (SELECT COUNT(*) FROM saves AS sv
                 WHERE sv.drop_id = d.id AND sv.created_at >= :since) AS saves_24h,
               (SELECT COUNT(*) FROM saves AS sv
                 WHERE sv.drop_id = d.id AND sv.is_pick = 1 AND sv.created_at >= :since) AS picks_24h
        FROM drops AS d
        JOIN songs AS s ON s.id = d.song_id
        JOIN users AS u ON u.id = d.user_id
        ORDER BY d.created_at DESC
    """), {"since": now - timedelta(hours=24)}).mappings()
    drops = [
        {**row, "created_at": iso(row["created_at"]), "expires_at": iso(row["expires_at"])}
        for row in rows
    ]
    return {"generated_at": iso(now), "drops": drops}


def get_drop_swipes(db: Session, drop_id: int) -> dict:
    """Who liked, picked or skipped one drop, the most recent first."""
    rows = db.execute(text("""
        SELECT * FROM (
            SELECT sv.created_at AS at, CASE WHEN sv.is_pick = 1 THEN 'pick' ELSE 'like' END AS type,
                   u.id AS user_id, u.username, 0 AS rescued
            FROM saves AS sv JOIN users AS u ON u.id = sv.user_id
            WHERE sv.drop_id = :drop_id
            UNION ALL
            -- rescued: skipped, then taken back from "Mes skips" after the drop expired
            SELECT sk.created_at, 'skip', u.id, u.username,
                   EXISTS (SELECT 1 FROM liked_songs AS l
                            WHERE l.user_id = sk.user_id AND l.song_id = d.song_id AND l.liked_late = 1)
            FROM skips AS sk
            JOIN users AS u ON u.id = sk.user_id
            JOIN drops AS d ON d.id = sk.drop_id
            WHERE sk.drop_id = :drop_id
        ) AS swipes
        ORDER BY at DESC
    """), {"drop_id": drop_id}).mappings()
    swipes = [{**row, "at": iso(row["at"])} for row in rows]
    return {"drop_id": drop_id, "swipes": swipes}