"""Data received and sent by the auth routes."""

from pydantic import BaseModel, EmailStr, Field


class UserRegister(BaseModel):
    """Data needed to create an account."""

    email: EmailStr
    username: str
    password: str = Field(min_length=8, max_length=128)


class UserOut(BaseModel):
    """Account data sent back by the API, without the password."""

    model_config = {"from_attributes": True}

    id: int
    email: str
    username: str


class UserLogin(BaseModel):
    """Data needed to log in, with an email or a username."""

    identifier: str = Field(min_length=1)
    password: str = Field(max_length=128)


class Token(BaseModel):
    """Token sent back after a successful login."""

    access_token: str
    token_type: str = "bearer"