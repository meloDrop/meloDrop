from sqlalchemy import Column, DateTime, Float, ForeignKey, Index, Integer, func

from app.database import Base


class Drop(Base):
    """Model for the 'drops' table."""

    __tablename__ = "drops"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(
        Integer,
        ForeignKey("users.id", ondelete="CASCADE"),
        nullable=False,
    )
    song_id = Column(
        Integer,
        ForeignKey("songs.id"),
        nullable=False,
    )
    expires_at = Column(DateTime, nullable=False)
    created_at = Column(DateTime, server_default=func.now())
    rand_key = Column(Float, nullable=False)

    # Index on the 'rand_key' column to speed up queries and sorting.
    # Naming convention: idx_[table_name]_[column_name]
    __table_args__ = (Index("idx_drops_rand_key", "rand_key"),)