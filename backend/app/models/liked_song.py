"""Model of the liked_songs table."""

from sqlalchemy import Column, DateTime, ForeignKey, Integer, UniqueConstraint, func

from app.database import Base


class LikedSong(Base):
    """A song in a user's "Mes likes" list."""

    __tablename__ = "liked_songs"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(
        Integer,
        ForeignKey("users.id", ondelete="CASCADE", name="fk_liked_songs_user_id"),
        nullable=False,
    )
    song_id = Column(
        Integer,
        ForeignKey("songs.id", name="fk_liked_songs_song_id"),
        nullable=False,
    )
    created_at = Column(DateTime, server_default=func.now())

    __table_args__ = (
        UniqueConstraint("user_id", "song_id", name="uq_liked_songs_user_song"),
    )