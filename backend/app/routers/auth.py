"""Routes of the auth module."""

from fastapi import APIRouter, Depends, HTTPException, Response
from sqlalchemy.orm import Session

from app.database import get_db
from app.dependencies import current_user
from app.exceptions import (
    EmailNotVerifiedError,
    InvalidCodeError,
    InvalidCredentialsError,
    InvalidPasswordError,
    UserAlreadyExistsError,
)
from app.models.user import User
from app.schemas import auth as schemas
from app.services import auth as services


router = APIRouter(prefix="/auth", tags=["auth"])
me_router = APIRouter(prefix="/me", tags=["auth"])


@router.post(
    "/register",
    status_code=201,
    response_model=schemas.UserOut,
    responses={
        201: {"description": "User created, verification code sent by email"},
        400: {"description": "Password doesn't validate complexity rules"},
        409: {"description": "Email or username already taken"},
    },
)
def register(body: schemas.UserRegister, db: Session = Depends(get_db)):
    """Create an account and email a verification code."""
    try:
        user = services.register_user(db, body.email, body.username, body.password)
    except InvalidPasswordError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except UserAlreadyExistsError as e:
        raise HTTPException(status_code=409, detail=str(e))

    return user


@router.post(
    "/verify-email",
    status_code=204,
    responses={
        204: {"description": "Email confirmed"},
        400: {"description": "Code invalid, expired or tried too many times"},
    },
)
def verify_email(body: schemas.EmailVerify, db: Session = Depends(get_db)):
    """Confirm the email of a new account with the code received."""
    try:
        services.verify_email(db, body.email, body.code)
    except InvalidCodeError as e:
        raise HTTPException(status_code=400, detail=str(e))
    return Response(status_code=204)


@router.post(
    "/resend-code",
    status_code=204,
    responses={204: {"description": "New code sent if the account exists and is not confirmed"}},
)
def resend_code(body: schemas.CodeRequest, db: Session = Depends(get_db)):
    """Send a new verification code."""
    services.resend_code(db, body.email)
    return Response(status_code=204)


@router.post(
    "/login",
    response_model=schemas.Token,
    responses={
        200: {"description": "Logged in, returns a JWT"},
        401: {"description": "Invalid credentials"},
        403: {"description": "Email not confirmed yet"},
    },
)
def login(body: schemas.UserLogin, db: Session = Depends(get_db)):
    """Log in and get a token."""
    try:
        user_token = services.login_user(db, identifier=body.identifier, password=body.password)
    except InvalidCredentialsError as e:
        raise HTTPException(status_code=401, detail=str(e))
    except EmailNotVerifiedError as e:
        raise HTTPException(status_code=403, detail=str(e))

    return schemas.Token(access_token=user_token)


@router.post(
    "/forgot-password",
    status_code=204,
    responses={204: {"description": "Reset code sent if the account exists"}},
)
def forgot_password(body: schemas.CodeRequest, db: Session = Depends(get_db)):
    """Send a code to reset a forgotten password."""
    services.forgot_password(db, body.email)
    return Response(status_code=204)


@router.post(
    "/reset-password",
    status_code=204,
    responses={
        204: {"description": "Password changed"},
        400: {"description": "Code invalid, expired or tried too many times, or new password too weak"},
    },
)
def reset_password(body: schemas.PasswordReset, db: Session = Depends(get_db)):
    """Change a forgotten password with the code received."""
    try:
        services.reset_password(db, body.email, body.code, body.new_password)
    except InvalidPasswordError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except InvalidCodeError as e:
        raise HTTPException(status_code=400, detail=str(e))
    return Response(status_code=204)


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