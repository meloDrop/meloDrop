"""Model of the labeled_songs table."""

from sqlalchemy import Column, DateTime, ForeignKey, Integer, String, UniqueConstraint, func

from app.database import Base


class LabeledSong(Base):
    """A song a user put on their Mixtape under a label, like "Le son qui me fait chialer"."""

    __tablename__ = "labeled_songs"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(
        Integer,
        ForeignKey("users.id", ondelete="CASCADE", name="fk_labeled_songs_user_id"),
        nullable=False,
    )
    song_id = Column(
        Integer,
        ForeignKey("songs.id", name="fk_labeled_songs_song_id"),
        nullable=False,
    )
    # Code of the label, like "cry": the app shows the text. The list of codes is
    # checked by Pydantic, so it can change without a migration
    label = Column(String(30), nullable=False)
    created_at = Column(DateTime, server_default=func.now())

    __table_args__ = (
        # One song per label for a user. The same song can be under two labels
        UniqueConstraint("user_id", "label", name="uq_labeled_songs_user_label"),
    )
