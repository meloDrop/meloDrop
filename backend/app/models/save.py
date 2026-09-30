from sqlalchemy import Column, Integer, Enum, DateTime, UniqueConstraint, ForeignKey
from sqlalchemy.sql import func
from app.database import Base

class Save(Base):
    __tablename__ = "saves"

    id         = Column(Integer, primary_key=True, autoincrement=True)
    user_id    = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    drop_id    = Column(Integer, ForeignKey("drops.id", ondelete="CASCADE"), nullable=False)
    provider   = Column(Enum("spotify", "deezer"), nullable=False)
    created_at = Column(DateTime, server_default=func.now())

    __table_args__ = (
        UniqueConstraint("user_id", "drop_id"),
    )