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
    created_at: datetime


class AdminEmailUpdate(BaseModel):
    """New email set by the admin, without code."""

    new_email: EmailStr


class AdminDeleteConfirm(BaseModel):
    """Username typed again to confirm an account deletion."""

    username: str