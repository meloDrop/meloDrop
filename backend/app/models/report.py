"""Model of the reports table."""

from sqlalchemy import Column, DateTime, ForeignKey, Integer, String, func

from app.database import Base


class Report(Base):
    """A profile reported by a user from the app, handled from the Signalements tab of the dashboard."""

    __tablename__ = "reports"

    id = Column(Integer, primary_key=True, autoincrement=True)
    # The user who sent the report. NULL once their account is deleted: the report stays
    reporter_id = Column(
        Integer,
        ForeignKey("users.id", ondelete="SET NULL", name="fk_reports_reporter_id"),
        nullable=True,
    )
    # The reported user. NULL once their account is deleted: the report stays in the history
    reported_id = Column(
        Integer,
        ForeignKey("users.id", ondelete="SET NULL", name="fk_reports_reported_id"),
        nullable=True,
    )
    # @username of the reported user when the report was sent, kept after a deletion
    reported_username = Column(String(30), nullable=False)
    # Code of the reason chosen in the app (ReportReason in schemas/reports.py)
    reason = Column(String(20), nullable=False)
    # What the user wrote to explain, or NULL
    message = Column(String(500), nullable=True)
    # open: to handle, resolved: handled, dismissed: closed without action
    status = Column(String(20), nullable=False, server_default="open")
    # Note of the admin: what was done, or why it was closed
    note = Column(String(300), nullable=True)
    created_at = Column(DateTime, server_default=func.now())
    # When the report was closed (resolved or dismissed), NULL while open
    closed_at = Column(DateTime, nullable=True)
