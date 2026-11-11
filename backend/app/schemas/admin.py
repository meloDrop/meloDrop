"""Data received and sent by the admin routes."""

from datetime import datetime

from pydantic import BaseModel, EmailStr


class AdminUserOut(BaseModel):
    """Account data shown to the admin."""

    model_config = {"from_attributes": True}

    id: int
    username: str
    email: str
    email_verified: bool
    role: str
    # True when the user has the subscription: premium cosmetics unlocked
    is_subscriber: bool = False
    created_at: datetime


class AdminSubscriberUpdate(BaseModel):
    """Subscriber status given or taken back from the dashboard."""

    is_subscriber: bool


class AdminEmailUpdate(BaseModel):
    """New email set by the admin, without code."""

    new_email: EmailStr


class AdminDeleteConfirm(BaseModel):
    """Username typed again to confirm an account deletion."""

    username: str