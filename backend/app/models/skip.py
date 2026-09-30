from sqlalchemy import Column, Integer, DateTime, ForeignKey, UniqueConstraint, func

from app.database import Base


class Skip(Base):
    __tablename__ = "skips"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(
        Integer,
        ForeignKey("users.id", ondelete="CASCADE", name="fk_skips_user_id"),
        nullable=False,
    )
    drop_id = Column(
        Integer,
        ForeignKey("drops.id", ondelete="CASCADE", name="fk_skips_drop_id"),
        nullable=False,
    )
    created_at = Column(DateTime, server_default=func.now())

    __table_args__ = (
        UniqueConstraint("user_id", "drop_id", name="uq_skips_user_drop"),
    )
