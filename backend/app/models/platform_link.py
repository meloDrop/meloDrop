"""Model of the platform_links table."""

from sqlalchemy import Column, Integer, String, Text, Enum, DateTime, BigInteger, ForeignKey, UniqueConstraint

from app.database import Base


class PlatformLink(Base):
    """A streaming account (Deezer or Spotify) linked to a user."""

    __tablename__ = "platform_links"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(
        Integer,
        ForeignKey("users.id", ondelete="CASCADE", name="fk_platform_links_user_id"),
        nullable=False,
    )
    provider = Column(Enum("spotify", "deezer"), nullable=False)
    provider_user_id = Column(String(255), nullable=False)
    access_token = Column(Text, nullable=False)
    refresh_token = Column(Text)
    expires_at = Column(DateTime)
    deezer_playlist_id = Column(BigInteger)

    __table_args__ = (
        UniqueConstraint("user_id", "provider", name="uq_platform_links_user_provider"),
    )