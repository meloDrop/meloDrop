"""Model of the playlists table."""

from sqlalchemy import Column, DateTime, ForeignKey, Integer, String, UniqueConstraint, func

from app.database import Base


class Playlist(Base):
    """A playlist created by a user to store liked tracks."""

    __tablename__ = "playlists"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(
        Integer,
        ForeignKey("users.id", ondelete="CASCADE", name="fk_playlists_user_id"),
        nullable=False,
    )
    name = Column(String(50), nullable=False)
    created_at = Column(DateTime, server_default=func.now())

    __table_args__ = (
        UniqueConstraint("user_id", "name", name="uq_playlists_user_name"),
    )