"""Model of the follows table."""

from sqlalchemy import Column, DateTime, ForeignKey, Integer, UniqueConstraint, func

from app.database import Base


class Follow(Base):
    """A user who follows another one: the drops of the followed user can come first in Discover."""

    __tablename__ = "follows"

    id = Column(Integer, primary_key=True, autoincrement=True)
    # The user who follows
    follower_id = Column(
        Integer,
        ForeignKey("users.id", ondelete="CASCADE", name="fk_follows_follower_id"),
        nullable=False,
    )
    # The user who is followed
    followed_id = Column(
        Integer,
        ForeignKey("users.id", ondelete="CASCADE", name="fk_follows_followed_id"),
        nullable=False,
    )
    created_at = Column(DateTime, server_default=func.now())

    __table_args__ = (
        # A user follows another one once. Following oneself is refused by the service:
        # MySQL does not allow a CHECK on columns with ON DELETE CASCADE
        UniqueConstraint("follower_id", "followed_id", name="uq_follows_follower_followed"),
    )
