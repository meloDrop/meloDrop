"""Data received and sent by the auth routes."""

from pydantic import BaseModel, EmailStr, Field

from datetime import datetime



class UserRegister(BaseModel):
    """Data needed to create an account."""

    email: EmailStr
    username: str = Field(min_length=3, max_length=30, pattern=r"^[^@]+$")
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


class LinkedAccountOut(BaseModel):
    """A streaming account linked to the user."""

    model_config = {"from_attributes": True}

    id: int
    provider: str

class MeOut(BaseModel):
    """Data of the logged-in account, with its linked accounts."""

    model_config = {"from_attributes": True}

    id: int
    email: str
    username: str
    created_at: datetime
    linked_accounts: list[LinkedAccountOut] = Field(validation_alias="links")


class EmailUpdate(BaseModel):
    """Data needed to change the email."""

    password: str = Field(max_length=128)
    new_email: EmailStr


class PasswordUpdate(BaseModel):
    """Data needed to change the password."""

    old_password: str = Field(max_length=128)
    # Same constraint as in UserRegister
    new_password: str = Field(
        min_length=8,
        max_length=128,
    )

class EmailVerify(BaseModel):
    """Email and 6-digit code, to confirm the email of a new account."""

    email: EmailStr
    # A string, not a number: a code like 048213 must keep its first 0
    code: str = Field(pattern=r"^\d{6}$")

class CodeRequest(BaseModel):
    """Email to send a code to (new verification code or forgotten password)."""

    email: EmailStr

class PasswordReset(BaseModel):
    """Email, 6-digit code and new password, to reset a forgotten password."""

    email: EmailStr
    code: str = Field(pattern=r"^\d{6}$")
    new_password: str = Field(min_length=8, max_length=128)