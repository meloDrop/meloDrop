from sqlalchemy import Column, ForeignKey, Integer

from app.database import Base


class DropGenre(Base):
    __tablename__ = "drop_genres"

    drop_id = Column(
        Integer,
        ForeignKey("drops.id", ondelete="CASCADE", name="fk_drop_genres_drop_id"),
        primary_key=True,
    )
    genre_id = Column(
        Integer,
        ForeignKey("genres.id", name="fk_drop_genres_genre_id"),
        primary_key=True,
    )
