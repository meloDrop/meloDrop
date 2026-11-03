"""Stats of a period to share with the testers, for the Récap tab of the admin dashboard (read only).

Only public data: @usernames and avatars, never an email.
The SQL only reads rows; the counting is done in Python, easier to follow.
"""

from collections import Counter, defaultdict
from datetime import datetime, timedelta
from math import sqrt

from sqlalchemy import text
from sqlalchemy.orm import Session

from app.utils.time import iso, utc_now

# Lines of each ranking
TOP_SIZE = 3
# Swipes needed on someone's drops to take part in "La meilleure oreille"
MIN_SWIPES_FOR_RATE = 5
# Likes needed, without any skip, for "L'unanimité"
MIN_LIKES_UNANIMOUS = 3


def prudent_rate(likes: int, swipes: int) -> float:
    """Low end of the range where the real like rate probably is (Wilson score, 95 %).

    2 likes out of 2 gives about 0.34, 18 out of 20 about 0.70:
    a rate seen on many swipes wins over a lucky start.
    """
    if swipes == 0:
        return 0
    z = 1.96
    p = likes / swipes
    return (p + z * z / (2 * swipes) - z * sqrt((p * (1 - p) + z * z / (4 * swipes)) / swipes)) / (1 + z * z / swipes)


def rows(db: Session, sql: str, params: dict) -> list[dict]:
    """Run a SELECT and return its rows as dictionaries."""
    return [dict(row) for row in db.execute(text(sql), params).mappings()]


def public_user(user: dict) -> dict:
    """What the app shows of a user to the others."""
    return {
        "id": user["id"],
        "username": user["username"],
        "avatar_color": user["avatar_color"],
        "avatar_style": user["avatar_style"],
    }


def public_drop(drop: dict, users: dict) -> dict:
    """A drop with its song and its author, without private data."""
    return {
        "drop_id": drop["id"],
        "song_id": drop["song_id"],
        "title": drop["title"],
        "artist": drop["artist"],
        "cover_url": drop["cover_url"],
        "genre": drop["genre"],
        "author": public_user(users[drop["author_id"]]),
    }


def get_recap(db: Session, days: int | None) -> dict:
    """Count everything for the period (None: since the start), and return one dictionary."""
    # Dates are stored in UTC without timezone (ADR-0001)
    now = utc_now()
    # "Since the start": a date before the first account
    since = now - timedelta(days=days) if days else datetime(2000, 1, 1)
    params = {"since": since}

    users = {u["id"]: u for u in rows(db, """
        SELECT id, username, avatar_color, avatar_style, email_verified, created_at FROM users
    """, {})}
    drops = {d["id"]: d for d in rows(db, """
        SELECT d.id, d.user_id AS author_id, d.created_at, s.id AS song_id, s.title,
               s.artist_name AS artist, s.cover_url, s.genre
        FROM drops AS d
        JOIN songs AS s ON s.id = d.song_id
    """, {})}
    # Every like and skip of the period
    swipes = rows(db, """
        SELECT drop_id, user_id, created_at, 1 AS is_like FROM saves WHERE created_at >= :since
        UNION ALL
        SELECT drop_id, user_id, created_at, 0 FROM skips WHERE created_at >= :since
    """, params)
    likes = [s for s in swipes if s["is_like"]]
    new_drops = [d for d in drops.values() if d["created_at"] >= since]
    liked_songs = rows(db, """
        SELECT liked_late FROM liked_songs WHERE created_at >= :since
    """, params)

    # ---------- Totals ----------
    active = {d["author_id"] for d in new_drops} | {s["user_id"] for s in swipes}
    totals = {
        "drops": len(new_drops),
        "likes": len(likes),
        "skips": len(swipes) - len(likes),
        "active_users": len(active),
        "testers": sum(1 for u in users.values() if u["email_verified"]),
        "new_testers": sum(1 for u in users.values() if u["email_verified"] and u["created_at"] >= since),
        "liked_songs": len(liked_songs),
        "rescues": sum(1 for row in liked_songs if row["liked_late"]),
        "songs": len({d["song_id"] for d in new_drops}),
        "artists": len({d["artist"] for d in new_drops}),
    }

    # ---------- Per drop, per author, per swiper ----------
    per_drop = defaultdict(lambda: {"likes": 0, "skips": 0})
    per_author = defaultdict(lambda: {"likes": 0, "skips": 0})
    likes_given = Counter()
    # likes_between[(liker, author)]: likes of a user on the drops of another
    likes_between = Counter()
    for swipe in swipes:
        drop = drops[swipe["drop_id"]]
        kind = "likes" if swipe["is_like"] else "skips"
        per_drop[drop["id"]][kind] += 1
        per_author[drop["author_id"]][kind] += 1
        if swipe["is_like"]:
            likes_given[swipe["user_id"]] += 1
            likes_between[(swipe["user_id"], drop["author_id"])] += 1

    # Podium: most likes in the period, then fewer skips, then the newest
    ranked = sorted(
        (d for d in per_drop if per_drop[d]["likes"] > 0),
        key=lambda d: (-per_drop[d]["likes"], per_drop[d]["skips"], -drops[d]["created_at"].timestamp()),
    )
    podium = [{**public_drop(drops[d], users), **per_drop[d]} for d in ranked[:TOP_SIZE]]

    # Unanimity: the most liked drop without any skip, not already on the podium
    unanimous = [
        d for d in ranked[TOP_SIZE:]
        if per_drop[d]["skips"] == 0 and per_drop[d]["likes"] >= MIN_LIKES_UNANIMOUS
    ]
    unanimity = {**public_drop(drops[unanimous[0]], users), **per_drop[unanimous[0]]} if unanimous else None

    # Dénicheurs: most likes received on their drops
    finders = sorted((a for a in per_author if per_author[a]["likes"] > 0),
                     key=lambda a: (-per_author[a]["likes"], users[a]["username"].lower()))
    drops_by_author = Counter(d["author_id"] for d in new_drops)
    top_finders = [
        {**public_user(users[a]), "likes": per_author[a]["likes"], "drops": drops_by_author[a]}
        for a in finders[:TOP_SIZE]
    ]

    # Best ear: best prudent like rate on their drops, with enough swipes to mean something
    rated = [a for a in per_author if sum(per_author[a].values()) >= MIN_SWIPES_FOR_RATE]
    best_ear = None
    if rated:
        best = max(rated, key=lambda a: prudent_rate(per_author[a]["likes"], sum(per_author[a].values())))
        best_ear = {**public_user(users[best]), **per_author[best]}

    # Generous: most likes given
    generous = [{**public_user(users[u]), "likes": n}
                for u, n in sorted(likes_given.items(), key=lambda item: (-item[1], users[item[0]]["username"].lower()))[:TOP_SIZE]]

    # Duo: two testers who like each other's drops, the most likes between them
    duo = None
    for (liker, author), count in likes_between.items():
        back = likes_between.get((author, liker), 0)
        if liker < author and back:
            score = (count + back, min(count, back))
            if duo is None or score > duo["score"]:
                duo = {"score": score, "users": [public_user(users[liker]), public_user(users[author])],
                       "likes": [count, back]}
    if duo:
        duo["total"] = duo.pop("score")[0]

    # Tube: the song dropped by the most different testers in the period
    droppers = defaultdict(set)
    for d in new_drops:
        droppers[d["song_id"]].add(d["author_id"])
    tube = None
    if droppers:
        song_id = max(droppers, key=lambda s: (len(droppers[s]), sum(1 for d in new_drops if d["song_id"] == s)))
        if len(droppers[song_id]) >= 2:
            sample = next(d for d in new_drops if d["song_id"] == song_id)
            tube = {**public_drop(sample, users), "droppers": len(droppers[song_id])}

    # Artists and genres: likes received by their drops in the period
    artist_likes = Counter(drops[s["drop_id"]]["artist"] for s in likes)
    genre_likes = Counter(drops[s["drop_id"]]["genre"] for s in likes if drops[s["drop_id"]]["genre"])

    # Swipes per UTC hour: the page turns them into French time
    hourly = Counter(s["created_at"].strftime("%Y-%m-%dT%H:00:00Z") for s in swipes)

    return {
        "generated_at": iso(now),
        "since": iso(since) if days else None,
        "days": days,
        "totals": totals,
        "podium": podium,
        "unanimity": unanimity,
        "finders": top_finders,
        "best_ear": best_ear,
        "generous": generous,
        "duo": duo,
        "tube": tube,
        "artists": [{"artist": a, "likes": n} for a, n in artist_likes.most_common(TOP_SIZE)],
        "genres": [{"genre": g, "likes": n} for g, n in genre_likes.most_common()],
        "genre_likes_total": sum(genre_likes.values()),
        "hourly": dict(hourly),
    }