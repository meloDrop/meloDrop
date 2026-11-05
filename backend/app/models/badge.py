"""Model of the badges table."""

from sqlalchemy import Column, DateTime, Integer, String, UniqueConstraint, func

from app.database import Base


class Badge(Base):
    """A badge created from the dashboard, like "Testeur de la 1re heure"."""

    __tablename__ = "badges"

    id = Column(Integer, primary_key=True, autoincrement=True)
    # Shown under the badge on the Mixtape
    name = Column(String(40), nullable=False)
    # Why the users got it, shown when the badge is tapped
    description = Column(String(200), nullable=False)
    # Codes of the icon and of the color, like "flame" and "peach". The lists of
    # codes are checked by Pydantic, so they can change without a migration
    icon = Column(String(20), nullable=False)
    color = Column(String(20), nullable=False)
    # Automatic badge: the code of the rule (BadgeRule in schemas/badges.py) and its threshold,
    # like "drops" and 10. NULL for a badge given by hand from the dashboard
    rule = Column(String(30), nullable=True)
    threshold = Column(Integer, nullable=True)
    created_at = Column(DateTime, server_default=func.now())

    __table_args__ = (
        # Two badges with the same name could not be told apart on a profile
        UniqueConstraint("name", name="uq_badges_name"),
    )
