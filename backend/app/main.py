from fastapi import FastAPI
from app.routers import genres

app = FastAPI(
    title="meloDrop API",
    version="0.1.0",
)

app.include_router(genres.router)

@app.get("/health")
def health():
    return {"status": "ok"}