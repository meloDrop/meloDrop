from fastapi import FastAPI, Depends
from fastapi.responses import JSONResponse
from sqlalchemy.orm import Session
from sqlalchemy import text

from app.database import get_db
from app.routers import auth, health, catalog, songs, drops, feed, swipes, likes, profile


app = FastAPI(
    title="meloDrop API",
    description="API Backend of the MeloDrop App",
    swagger_ui_parameters={"syntaxHighlight.theme": "obsidian"}
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


# Profiles: the user's own and the others'
app.include_router(profile.router)