"""Search the Deezer catalog, or look at one track, for the Catalogue tab of the admin dashboard."""

from sqlalchemy import text
from sqlalchemy.orm import Session

from app.providers.deezer import DeezerProvider
from app.services.drops import get_album_genre

# Talks to the public Deezer API
deezer_provider = DeezerProvider()


def songs_in_base(db: Session, deezer_ids: list[int], isrcs: list[str]) -> list[dict]:
    """Songs of the base with one of these Deezer ids or ISRCs, with their counters."""
    if not deezer_ids and not isrcs:
        return []
    # One :name per value, the safe way to write an IN (...) with text()
    params = {f"d{i}": value for i, value in enumerate(deezer_ids)}
    params.update({f"i{i}": value for i, value in enumerate(isrcs)})
    conditions = []
    if deezer_ids:
        conditions.append(f"s.deezer_track_id IN ({', '.join(f':d{i}' for i in range(len(deezer_ids)))})")
    if isrcs:
        conditions.append(f"s.isrc IN ({', '.join(f':i{i}' for i in range(len(isrcs)))})")
    rows = db.execute(text(f"""
        SELECT s.id AS song_id, s.deezer_track_id, s.isrc, s.genre,
               (SELECT COUNT(*) FROM drops AS d WHERE d.song_id = s.id) AS drops,
               (SELECT COUNT(*) FROM saves AS sv JOIN drops AS d ON d.id = sv.drop_id
                 WHERE d.song_id = s.id) AS likes,
               (SELECT COUNT(*) FROM liked_songs AS l WHERE l.song_id = s.id) AS in_likes
        FROM songs AS s
        WHERE {' OR '.join(conditions)}
    """), params).mappings()
    return [dict(row) for row in rows]


def search_catalog(db: Session, query: str) -> dict:
    """Tracks found by Deezer, in Deezer's order, like in the app."""
    # A DeezerError goes up to the router
    results = deezer_provider.search(query)
    in_base = {row["deezer_track_id"]: row for row in songs_in_base(db, [t["id"] for t in results], [])}
    tracks = []
    for track in results:
        tracks.append({
            "deezer_track_id": track["id"],
            "title": track["title"],
            "artist": track["artist"]["name"],
            "album": track.get("album", {}).get("title"),
            # Same cover as the search of the app
            "cover_url": track.get("album", {}).get("cover_medium") or None,
            "preview_url": track.get("preview") or None,
            "duration": track.get("duration"),
            # Popularity on Deezer, the higher the better
            "rank": track.get("rank"),
            "explicit": bool(track.get("explicit_lyrics")),
            "in_base": in_base.get(track["id"]),
        })
    return {"query": query, "results": tracks}


def get_catalog_track(db: Session, track_id: int) -> dict:
    """Everything about one track, and what meloDrop would store at its first drop."""
    # A TrackNotFoundError or a DeezerError goes up to the router
    track = deezer_provider.get_track(track_id)
    isrc = track.get("isrc") or None
    album = track.get("album", {})

    # Same steps as find_or_create_song (ADR-0014): Deezer id, then ISRC
    matches = songs_in_base(db, [track["id"]], [isrc] if isrc else [])
    by_id = next((m for m in matches if m["deezer_track_id"] == track["id"]), None)
    by_isrc = next((m for m in matches if isrc and m["isrc"] == isrc), None)
    in_base = by_id or by_isrc

    # Only asked to Deezer when the song is new, like at drop time (ADR-0017)
    genre = in_base["genre"] if in_base else get_album_genre(album["id"]) if album.get("id") else None

    return {
        "track": {
            "deezer_track_id": track["id"],
            "title": track["title"],
            "artist": track["artist"]["name"],
            "contributors": [c["name"] for c in track.get("contributors", [])],
            "album": album.get("title"),
            "album_id": album.get("id"),
            "cover_url": album.get("cover_big") or None,
            "preview_url": track.get("preview") or None,
            "isrc": isrc,
            "duration": track.get("duration"),
            "release_date": track.get("release_date"),
            "bpm": track.get("bpm") or None,
            "rank": track.get("rank"),
            "explicit": bool(track.get("explicit_lyrics")),
            "link": track.get("link"),
            "genre": genre,
        },
        # "id": same Deezer id; "isrc": same song under another Deezer id
        "match": "id" if by_id else "isrc" if by_isrc else None,
        "in_base": in_base,
        # The row find_or_create_song would create if the song is new
        "would_store": {
            "title": track["title"],
            "artist_name": track["artist"]["name"],
            "cover_url": album.get("cover_big") or None,
            "isrc": isrc,
            "genre": genre,
            "deezer_track_id": track["id"],
        },
        # Raw answer of Deezer, for curious eyes
        "raw": track,
    }