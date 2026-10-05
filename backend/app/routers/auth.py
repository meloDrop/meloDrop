"""Routes of the auth module."""

from fastapi import APIRouter, Depends, HTTPException, Response
from sqlalchemy.orm import Session

from app.database import get_db
from app.dependencies import current_user
from app.exceptions import InvalidCredentialsError, UserAlreadyExistsError, InvalidPasswordError
from app.models.user import User
from app.schemas import auth as schemas
from app.services import auth as services

from app.utils import load_doc

router = APIRouter(prefix="/auth", tags=["auth"])
me_router = APIRouter(prefix="/me", tags=["auth"])


@router.post(
    "/register",
    status_code=201,
    response_model=schemas.UserOut,
    responses={
        201: {"description": "User created"},
        400: {"description": "Password doesn't validate complexity rules"},
        409: {"description": "Email or username already taken"},
    },
    description=load_doc("users_register.md"),
)
def register(body: schemas.UserRegister, db: Session = Depends(get_db)):
    """Create an account."""
    try:
        user = services.register_user(db, body.email, body.username, body.password)
    except InvalidPasswordError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except UserAlreadyExistsError as e:
        raise HTTPException(status_code=409, detail=str(e))

    return user


@router.post(
    "/login",
    response_model=schemas.Token,
    responses={
        200: {"description": "Logged in, returns a JWT"},
        401: {"description": "Invalid credentials"},
    },
    description=load_doc("users_login.md"),
)
def login(body: schemas.UserLogin, db: Session = Depends(get_db)):
    """Log in and get a token."""
    try:
        user_token = services.login_user(db, identifier=body.identifier, password=body.password)
    except InvalidCredentialsError as e:
        raise HTTPException(status_code=401, detail=str(e))

    return schemas.Token(access_token=user_token)


@me_router.get("", response_model=schemas.MeOut)
def get_me(user: User = Depends(current_user)):
    """Return the logged-in account."""
    return user


@me_router.patch(
    "/email",
    response_model=schemas.UserOut,
    responses={
        403: {"description": "Wrong password"},
        409: {"description": "Email already taken"},
    },
)
def update_email(
    body: schemas.EmailUpdate,
    user: User = Depends(current_user),
    db: Session = Depends(get_db),
):
    """Change the email of the logged-in account."""
    try:
        return services.change_email(db, user, body.password, body.new_email)
    except InvalidCredentialsError as e:
        raise HTTPException(status_code=403, detail=str(e))
    except UserAlreadyExistsError as e:
        raise HTTPException(status_code=409, detail=str(e))


@me_router.patch(
    "/password",
    status_code=204,
    responses={
        400: {"description": "New password doesn't validate complexity rules"},
        403: {"description": "Wrong old password"},
    },
)
def update_password(
    body: schemas.PasswordUpdate,
    user: User = Depends(current_user),
    db: Session = Depends(get_db),
):
    """Change the password of the logged-in account."""
    try:
        services.change_password(db, user, body.old_password, body.new_password)
    except InvalidCredentialsError as e:
        raise HTTPException(status_code=403, detail=str(e))
    except InvalidPasswordError as e:
        raise HTTPException(status_code=400, detail=str(e))
    return Response(status_code=204)


@me_router.delete("", status_code=204)
def delete_me(user: User = Depends(current_user), db: Session = Depends(get_db)):
    """Delete the logged-in account and all its data."""
    services.delete_user(db, user)
    return Response(status_code=204)
