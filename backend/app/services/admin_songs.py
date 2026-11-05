"""All the songs stored in meloDrop, for the Sons tab of the admin dashboard (read only)."""

from sqlalchemy import text
from sqlalchemy.orm import Session

from app.utils.time import iso, utc_now


def get_songs(db: Session) -> dict:
    """Every song stored in meloDrop, the newest first, with its drops and swipes."""
    # Dates are stored in UTC without timezone (ADR-0001)
    now = utc_now()
    rows = db.execute(text("""
        SELECT s.id AS song_id, s.title, s.artist_name AS artist, s.cover_url, s.genre,
               s.isrc, s.deezer_track_id, s.created_at,
               (SELECT COUNT(*) FROM drops AS d WHERE d.song_id = s.id) AS drops,
               (SELECT COUNT(DISTINCT d.user_id) FROM drops AS d WHERE d.song_id = s.id) AS droppers,
               (SELECT COUNT(*) FROM saves AS sv JOIN drops AS d ON d.id = sv.drop_id
                 WHERE d.song_id = s.id) AS saves,
               (SELECT COUNT(*) FROM saves AS sv JOIN drops AS d ON d.id = sv.drop_id
                 WHERE d.song_id = s.id AND sv.is_pick = 1) AS picks,
               (SELECT COUNT(*) FROM skips AS sk JOIN drops AS d ON d.id = sk.drop_id
                 WHERE d.song_id = s.id) AS skips,
               -- Users who still have the song in "Mes likes" (ADR-0013)
               (SELECT COUNT(*) FROM liked_songs AS l WHERE l.song_id = s.id) AS in_likes,
               -- Of them, users who have it as a pick ("Mes picks")
               (SELECT COUNT(*) FROM liked_songs AS l WHERE l.song_id = s.id AND l.is_pick = 1) AS in_picks,
               (SELECT MAX(d.created_at) FROM drops AS d WHERE d.song_id = s.id) AS last_drop
        FROM songs AS s
        ORDER BY s.created_at DESC
    """)).mappings()
    songs = [
        {**row, "created_at": iso(row["created_at"]), "last_drop": iso(row["last_drop"])}
        for row in rows
    ]
    return {"generated_at": iso(now), "songs": songs}