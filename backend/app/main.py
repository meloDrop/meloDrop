from fastapi import FastAPI, Depends
from fastapi.responses import JSONResponse
from sqlalchemy.orm import Session
from sqlalchemy import text

from app.database import get_db
from app.routers import genres, auth, health, catalog, songs, drops, feed, swipes, likes


app = FastAPI(
    title="meloDrop API",
    description="API Backend of the MeloDrop App",
    swagger_ui_parameters={"syntaxHighlight.theme": "obsidian"}
)

# Health route
app.include_router(health.router)

# Genre route
app.include_router(genres.router)

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
