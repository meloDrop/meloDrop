"""Data received and sent by the reports routes."""

from datetime import datetime
from typing import Literal

from pydantic import BaseModel, Field, model_validator

# Reasons a user can choose in the app, in the order of the form
ReportReason = Literal["name", "content", "spam", "harassment", "other"]

# open: to handle. resolved: handled (an action was done). dismissed: closed without action
ReportStatus = Literal["open", "resolved", "dismissed"]


class ReportCreate(BaseModel):
    """A report sent from the profile of another user."""

    # Spaces around the message are removed
    model_config = {"str_strip_whitespace": True}

    reason: ReportReason
    # What the user explains, optional except for "other"
    message: str | None = Field(default=None, max_length=500)

    @model_validator(mode="after")
    def check_message(self):
        """An empty message is no message. "other" needs a message to know what it is about."""
        if self.message == "":
            self.message = None
        if self.reason == "other" and self.message is None:
            raise ValueError("message_required")
        return self


class ReportSentOut(BaseModel):
    """What the app gets back once the report is saved."""

    id: int
    created_at: datetime


class ReportUserOut(BaseModel):
    """A user in a report, as the dashboard shows it."""

    id: int
    username: str
    display_name: str | None = None
    avatar_color: str
    avatar_style: str


class AdminReportOut(BaseModel):
    """A report in the Signalements tab of the dashboard."""

    id: int
    status: ReportStatus
    reason: str
    message: str | None = None
    note: str | None = None
    created_at: datetime
    closed_at: datetime | None = None
    # Null once the account is deleted
    reporter: ReportUserOut | None = None
    reported: ReportUserOut | None = None
    # @username of the reported user when the report was sent, kept after a deletion
    reported_username: str


class ReportUpdate(BaseModel):
    """Close a report (resolved or dismissed), or open it again."""

    model_config = {"str_strip_whitespace": True}

    status: ReportStatus
    # Note of the admin. Not sent: the note stays the same. Empty text: the note is removed
    note: str | None = Field(default=None, max_length=300)
