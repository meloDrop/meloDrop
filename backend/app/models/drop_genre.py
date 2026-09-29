from sqlalchemy import Column, ForeignKey, Integer

from app.database import Base

class DropGenre(Base):
    __tablename__ = 'drop_genres'

    drop_id = Column(
        Integer,
        ForeignKey("drops.id", ondelete="CASCADE"),
        primary_key=True,
        nullable=False,
    )
    genre_id = Column(
        Integer,
        ForeignKey("genres.id"),
        primary_key=True,
        nullable=False,
    )