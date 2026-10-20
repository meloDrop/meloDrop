"""Model of the songs table."""

from sqlalchemy import BigInteger, Column, Integer, String, DateTime, UniqueConstraint, func

from app.database import Base


class Song(Base):
    """A song from Deezer or Spotify, stored once and shared by all its drops."""

    __tablename__ = "songs"

    id = Column(Integer, primary_key=True, autoincrement=True)
    isrc = Column(String(12))
    title = Column(String(255), nullable=False)
    artist_name = Column(String(255), nullable=False)
    cover_url = Column(String(500))
    genre = Column(String(50), nullable=False)
    deezer_track_id = Column(BigInteger)
    spotify_track_id = Column(String(500))
    created_at = Column(DateTime, server_default=func.now())

    __table_args__ = (
        UniqueConstraint("isrc", name="uq_songs_isrc"),
        UniqueConstraint("deezer_track_id", name="uq_songs_deezer_track_id"),
    )
