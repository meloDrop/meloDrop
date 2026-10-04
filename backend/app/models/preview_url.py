"""Model of the preview_urls table."""

from sqlalchemy import Column, DateTime, Enum, ForeignKey, Integer, String, UniqueConstraint, func

from app.database import Base


class PreviewUrl(Base):
    """A link to a short audio extract of a song, refreshed after 12 hours."""

    __tablename__ = "preview_urls"

    id = Column(Integer, primary_key=True, autoincrement=True)
    song_id = Column(
        Integer,
        ForeignKey("songs.id", ondelete="CASCADE", name="fk_preview_urls_song_id"),
        nullable=False,
    )
    provider = Column(Enum("deezer", "spotify"), nullable=False)
    url = Column(String(500), nullable=False)
    fetched_at = Column(DateTime, nullable=False, server_default=func.now())

    __table_args__ = (
        UniqueConstraint("song_id", "provider", name="uq_preview_urls_song_provider"),
    )