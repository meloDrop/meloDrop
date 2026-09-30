from sqlalchemy import Column, ForeignKey, Integer, UniqueConstraint

from app.database import Base


class DropGenre(Base):
    __tablename__ = "drop_genres"

    id = Column(Integer, primary_key=True, autoincrement=True)
    drop_id = Column(
        Integer,
        ForeignKey("drops.id", ondelete="CASCADE", name="fk_drop_genres_drop_id"),
        nullable=False,
    )
    genre_id = Column(
        Integer,
        ForeignKey("genres.id", name="fk_drop_genres_genre_id"),
        nullable=False,
    )

    __table_args__ = (
        UniqueConstraint("drop_id", "genre_id", name="uq_drop_genres_drop_genre"),
    )
