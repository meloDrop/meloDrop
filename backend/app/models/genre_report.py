from sqlalchemy import Column, DateTime, ForeignKey, Integer, UniqueConstraint, func

from app.database import Base


class GenreReport(Base):
    __tablename__ = "genre_reports"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(
        Integer,
        ForeignKey("users.id", ondelete="CASCADE", name="fk_genre_reports_user_id"),
        nullable=False,
    )
    drop_id = Column(
        Integer,
        ForeignKey("drops.id", ondelete="CASCADE", name="fk_genre_reports_drop_id"),
        nullable=False,
    )
    created_at = Column(DateTime, server_default=func.now())

    __table_args__ = (
        UniqueConstraint("user_id", "drop_id", name="uq_genre_reports_user_drop"),
    )
