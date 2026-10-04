"""Model of the genres table."""

from sqlalchemy import Column, Integer, String, UniqueConstraint

from app.database import Base


class Genre(Base):
    """A music genre that can be added to a drop."""

    __tablename__ = "genres"

    id = Column(Integer, primary_key=True, autoincrement=True)
    label = Column(String(50), nullable=False)

    __table_args__ = (
        UniqueConstraint("label", name="uq_genres_label"),
    )