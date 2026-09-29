from sqlalchemy import Column, Integer, String, DateTime, func
from app.database import Base

class Song(Base):
    __tablename__ = "song"
    id = Column(Integer, primary_key=True)
    isrc = Column(String(12), unique=True)
    title = Column(String(255), nullable=False)
    artist_name = Column(String(255), nullable=False)
    cover_url = Column(String(500))
    deezer_track_id = Column(Integer, unique=True)
    spotify_track_id = Column(String(500))
    created_at = Column(DateTime, default=func.now())