from sqlalchemy import Column, Integer, String

from app.database import Base


class Genre(Base):
    __tablename__ = "genres"

    id = Column(Integer, primary_key=True)
    slug = Column(String(50), nullable=False, unique=True)
    label = Column(String(50), nullable=False)