"""Model of the users table."""

from sqlalchemy import Column, Integer, String, DateTime, UniqueConstraint, func, Boolean
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
    # False until the user types the code received by email
    email_verified = Column(Boolean, nullable=False, default=False)
    # New email waiting for the code sent to it, empty the rest of the time
    pending_email = Column(String(255), nullable=True)

    __table_args__ = (
        UniqueConstraint("email", name="uq_users_email"),
        UniqueConstraint("username", name="uq_users_username"),
    )