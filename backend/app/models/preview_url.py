from sqlalchemy import Column, DateTime, Enum, ForeignKey, Integer, String

from app.database import Base

class PreviewUrl(Base):
    __tablename__ = 'preview_urls'
    id = Column(Integer,primary_key=True, autoincrement=True)
    song_id = Column(Integer,
            ForeignKey('songs.id', ondelete='CASCADE'),
            nullable=False,
            unique=True)
    provider = Column(
        Enum("deezer", "spotify", name="provider_enum"),
        nullable=False,
    )
    url = Column(String(500), nullable=False)
    fetched_at = Column(DateTime, nullable=False,server_default=func.now(),)