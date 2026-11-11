"""Data sent back by the follow routes."""

from pydantic import BaseModel


class FollowUserOut(BaseModel):
    """A user in a list of followers or of followed users."""

    # Read the fields straight from the User object
    model_config = {"from_attributes": True}

    # Used to open the profile (GET /users/{id}/profile)
    id: int
    username: str
    # Public name, or null: the app then shows the username
    display_name: str | None = None
    avatar_color: str
    avatar_style: str
    # Cosmetics: color of the drop, background decoration, face and aura
    avatar_drop: str = "night"
    avatar_deco: str = "plain"
    avatar_face: str = "none"
    avatar_aura: str = "none"
