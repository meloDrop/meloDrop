"""Schemas for the links module."""

from pydantic import BaseModel


class DeezerStartOut(BaseModel):
    auth_url: str