"""Model of the users table."""

from sqlalchemy import Column, Integer, String, DateTime, UniqueConstraint, func
from sqlalchemy.orm import relationship

from app.database import Base


class User(Base):
    """A meloDrop account."""

    __tablename__ = "users"

    id = Column(Integer, primary_key=True, autoincrement=True)
    email = Column(String(255), nullable=False)
    username = Column(String(30), nullable=False)
    password_hash = Column(String(255), nullable=False)
    avatar_url = Column(String(255))
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=func.now(), onupdate=func.now())

    links = relationship("PlatformLink", passive_deletes=True)

    __table_args__ = (
        UniqueConstraint("email", name="uq_users_email"),
        UniqueConstraint("username", name="uq_users_username"),
    )