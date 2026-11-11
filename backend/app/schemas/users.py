"""Data sent back by the user search route."""

from pydantic import BaseModel


class UserSearchOut(BaseModel):
    """A user found by the search, as shown in the results list."""

    # Read the fields straight from the User object
    model_config = {"from_attributes": True}

    # Used to open the profile (GET /users/{id}/profile)
    id: int
    # The @handle the search is done on
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


class UserBriefOut(BaseModel):
    """A user in a short list: who dropped a song, who liked it."""

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
