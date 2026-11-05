"""Model of the podium_songs table."""

from sqlalchemy import CheckConstraint, Column, DateTime, ForeignKey, Integer, UniqueConstraint, func

from app.database import Base


class PodiumSong(Base):
    """A song on a user's podium: one of their 3 favorite songs of all time, shown on their Mixtape."""

    __tablename__ = "podium_songs"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(
        Integer,
        ForeignKey("users.id", ondelete="CASCADE", name="fk_podium_songs_user_id"),
        nullable=False,
    )
    song_id = Column(
        Integer,
        ForeignKey("songs.id", name="fk_podium_songs_song_id"),
        nullable=False,
    )
    # Place on the podium: 1, 2 or 3. Not "rank": RANK is a reserved word in MySQL
    place = Column(Integer, nullable=False)
    created_at = Column(DateTime, server_default=func.now())

    __table_args__ = (
        # One song per place, and the same song only once on a podium
        UniqueConstraint("user_id", "place", name="uq_podium_songs_user_place"),
        UniqueConstraint("user_id", "song_id", name="uq_podium_songs_user_song"),
        # The database refuses any other place, even if the code has a bug
        CheckConstraint("place BETWEEN 1 AND 3", name="ck_podium_songs_place"),
    )