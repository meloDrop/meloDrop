import logging
import time

from fastapi import FastAPI, Depends, Request
from fastapi.responses import JSONResponse
from sqlalchemy.orm import Session
from sqlalchemy import text

from app.database import get_db
from app.routers import auth, health, catalog, songs, drops, feed, swipes, likes, skips, picks, podium, labeled_songs, profile, admin, admin_badges, users
logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
logger = logging.getLogger("melodrop")


app = FastAPI(
    title="meloDrop API",
    description="API Backend of the MeloDrop App",
    swagger_ui_parameters={"syntaxHighlight.theme": "obsidian"}
)


@app.middleware("http")
async def log_requests(request: Request, call_next):
    """Write one log line per request, with the user who made it."""
    start = time.perf_counter()
    status_code = 500
    try:
        response = await call_next(request)
        status_code = response.status_code
        return response
    finally:
        if request.url.path != "/health":
            duration = (time.perf_counter() - start) * 1000
            username = getattr(request.state, "username", None)
            who = f"@{username}" if username else "anonyme"
            logger.info(
                "%s %s -> %s (%s, %.0f ms)",
                request.method, request.url.path, status_code, who, duration,
            )


# Health route
app.include_router(health.router)

# Authentication
app.include_router(auth.router)
app.include_router(auth.me_router)


# Catalog Deezer
app.include_router(catalog.router)

# Song preview
app.include_router(songs.router)

# Drops
app.include_router(drops.router)

# Feed
app.include_router(feed.router)

# Swipes (skip or save)
app.include_router(swipes.router)

# "Mes likes" list
app.include_router(likes.router)

# "Mes skips" list and rescue of a skipped song
app.include_router(skips.router)

# "Mes picks" list: a like can become a pick and back
app.include_router(picks.router)

# Podium of the Mixtape: the 3 favorite songs of all time
app.include_router(podium.router)

# Labeled songs of the Mixtape, like "Le son qui me fait chialer"
app.include_router(labeled_songs.router)

# Profiles: the user's own and the others'
app.include_router(profile.router)
app.include_router(profile.me_router)

# Search of other users by @username
app.include_router(users.router)

# Admin: actions on the accounts, admin role needed
app.include_router(admin.router)
app.include_router(admin.page_router)

# Badges: created in the dashboard, then given to the users
app.include_router(admin_badges.router)