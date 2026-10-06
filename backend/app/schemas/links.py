"""Schemas for the links module."""

from pydantic import BaseModel


class LinkStartOut(BaseModel):
    auth_url: str