"""Table email_codes: 6-digit codes sent by email."""

from sqlalchemy import Column, DateTime, Enum, ForeignKey, Integer, String, UniqueConstraint, func, text

from app.database import Base


class EmailCode(Base):
    """A code sent to a user, to verify their email or reset their password."""

    __tablename__ = "email_codes"
    __table_args__ = (
        UniqueConstraint("user_id", "purpose", name="uq_email_codes_user_id_purpose"),
    )

    id = Column(Integer, primary_key=True, autoincrement=True)
    # The code is deleted with the account
    user_id = Column(
        Integer,
        ForeignKey("users.id", ondelete="CASCADE", name="fk_email_codes_user_id"),
        nullable=False,
    )
    # Same values as kind in send_code_email
    purpose = Column(Enum("verify_email", "reset_password", "change_email"), nullable=False)
    # Only the hash of the code is stored, like for passwords
    code_hash = Column(String(255), nullable=False)
    # Number of wrong codes typed
    attempts = Column(Integer, nullable=False, server_default=text("0"))
    expires_at = Column(DateTime, nullable=False)
    created_at = Column(DateTime, nullable=False, server_default=func.now())
