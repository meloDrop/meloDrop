"""Model of the saves table."""

from sqlalchemy import Column, Integer, Enum, DateTime, UniqueConstraint, ForeignKey, func

from app.database import Base


class Save(Base):
    """A drop kept by a user and added to their streaming favorites."""

    __tablename__ = "saves"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(
        Integer,
        ForeignKey("users.id", ondelete="CASCADE", name="fk_saves_user_id"),
        nullable=False,
    )
    drop_id = Column(
        Integer,
        ForeignKey("drops.id", ondelete="CASCADE", name="fk_saves_drop_id"),
        nullable=False,
    )
    provider = Column(Enum("spotify", "deezer"), nullable=False)
    created_at = Column(DateTime, server_default=func.now())

    __table_args__ = (
        UniqueConstraint("user_id", "drop_id", name="uq_saves_user_drop"),
    )