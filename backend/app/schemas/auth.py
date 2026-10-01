from pydantic import BaseModel, EmailStr, Field


class UserRegister(BaseModel):
    email: EmailStr
    username: str
    password: str = Field(min_length=8, max_length=128)


class UserOut(BaseModel):
    model_config = {"from_attributes": True}

    id: int
    email: str
    username: str


class UserLogin(BaseModel):
    identifier: str = Field(min_length=1)
    password: str = Field(max_length=128)


class Token(BaseModel):
    access_token: str
    token_type: str = "bearer"