"""Model of the user_badges table."""

from sqlalchemy import Column, DateTime, ForeignKey, Integer, UniqueConstraint, func

from app.database import Base


class UserBadge(Base):
    """A badge given to a user from the dashboard, shown on their Mixtape."""

    __tablename__ = "user_badges"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(
        Integer,
        ForeignKey("users.id", ondelete="CASCADE", name="fk_user_badges_user_id"),
        nullable=False,
    )
    # A deleted badge disappears from every profile
    badge_id = Column(
        Integer,
        ForeignKey("badges.id", ondelete="CASCADE", name="fk_user_badges_badge_id"),
        nullable=False,
    )
    # When the badge was given, shown when the badge is tapped
    awarded_at = Column(DateTime, server_default=func.now())

    __table_args__ = (
        # A user has a badge once
        UniqueConstraint("user_id", "badge_id", name="uq_user_badges_user_badge"),
    )
