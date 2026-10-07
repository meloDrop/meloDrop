from fastapi import FastAPI, Depends
from fastapi.responses import JSONResponse
from sqlalchemy.orm import Session
from sqlalchemy import text

from app.database import get_db
from app.routers import genres, auth, health, catalog


app = FastAPI(
    title="meloDrop API",
    description="API Backend of the MeloDrop App",
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