from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.database import get_db
from app.exceptions import InvalidCredentialsError, UserAlreadyExistsError
from app.schemas.auth import Token, UserLogin, UserOut, UserRegister
from app.services.auth import login_user, register_user

from app.utils import load_doc

router = APIRouter(prefix="/auth", tags=["auth"])


@router.post(
    "/register",
    status_code=201,
    response_model=UserOut,
    responses={
        201: {"description": "User created"},
        409: {"description": "Email or username already taken"},
    },
    description=load_doc("users_register.md"),
)
def register(body: UserRegister, db: Session = Depends(get_db)):
    try:
        user = register_user(db, body.email, body.username, body.password)
    except UserAlreadyExistsError as e:
        raise HTTPException(status_code=409, detail=str(e))

    return user


@router.post(
    "/login",
    response_model=Token,
    responses={
        200: {"description": "Logged in, returns a JWT"},
        401: {"description": "Invalid credentials"},
    },
    description=load_doc("users_login.md"),
)
def login(body: UserLogin, db: Session = Depends(get_db)):
    try:
        user_token = login_user(db, identifier=body.identifier, password=body.password)
    except InvalidCredentialsError as e:
        raise HTTPException(status_code=401, detail=str(e))

    return Token(access_token=user_token)
