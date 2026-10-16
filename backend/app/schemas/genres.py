"""Data sent back by the genres routes."""

from pydantic import BaseModel


class GenreOut(BaseModel):
    """A genre from the closed list, with its id and label."""

    model_config = {"from_attributes": True}

    id: int
    label: str
