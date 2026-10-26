"""Model of the users table."""

from sqlalchemy import Column, Integer, String, DateTime, UniqueConstraint, func, Boolean, text

from app.database import Base


class User(Base):
    """A meloDrop account."""

    __tablename__ = "users"

    id = Column(Integer, primary_key=True, autoincrement=True)
    email = Column(String(255), nullable=False)
    username = Column(String(30), nullable=False)
    # Public name shown on the profile, free to change; empty = the username
    display_name = Column(String(30), nullable=True)
    password_hash = Column(String(255), nullable=False)
    # Avatar: a pastel background and a drop style, from closed lists
    avatar_color = Column(String(20), nullable=False, server_default="lavender")
    avatar_style = Column(String(20), nullable=False, server_default="simple")
    created_at = Column(DateTime, server_default=func.now())
    updated_at = Column(DateTime, server_default=text("CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"), onupdate=func.now())

    # False until the user types the code received by email
    email_verified = Column(Boolean, nullable=False, server_default=text("0"))
    # New email waiting for the code sent to it, empty the rest of the time
    pending_email = Column(String(255), nullable=True)

    __table_args__ = (
        UniqueConstraint("email", name="uq_users_email"),
        UniqueConstraint("username", name="uq_users_username"),
    )