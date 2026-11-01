"""Routes of the admin module."""

from fastapi import APIRouter, Depends, HTTPException, Path
from sqlalchemy.orm import Session

from app.database import get_db
from app.dependencies import current_admin
from app.exceptions import (
    DropNotFoundError,
    OwnAccountError,
    UserAlreadyExistsError,
    UserNotFoundError,
    UsernameMismatchError,
)
from app.models.user import User
from app.schemas import admin as schemas
from app.services import admin as services

# Every route needs an admin: 401 if not logged in, 403 if not admin
router = APIRouter(prefix="/admin", tags=["admin"], dependencies=[Depends(current_admin)])


@router.get(
    "/users",
    response_model=list[schemas.AdminUserOut],
    responses={200: {"description": "Every account, the newest first"}},
)
def list_users(db: Session = Depends(get_db)):
    """List every account, to find the id of the one to manage."""
    return services.list_users(db)


@router.patch(
    "/users/{user_id}/email",
    response_model=schemas.AdminUserOut,
    responses={
        200: {"description": "Email replaced and marked verified"},
        404: {"description": "No user with this id"},
        409: {"description": "Email already taken, or same as the current one"},
    },
)
def set_email(
    body: schemas.AdminEmailUpdate,
    user_id: int = Path(description="meloDrop user id (id in GET /admin/users)"),
    db: Session = Depends(get_db),
):
    """Replace a user's email at once, without code."""
    try:
        return services.set_email(db, user_id, body.new_email)
    except UserNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except UserAlreadyExistsError as e:
        raise HTTPException(status_code=409, detail=str(e))


@router.post(
    "/users/{user_id}/verify-email",
    response_model=schemas.AdminUserOut,
    responses={
        200: {"description": "Email marked verified"},
        404: {"description": "No user with this id"},
    },
)
def verify_email(
    user_id: int = Path(description="meloDrop user id (id in GET /admin/users)"),
    db: Session = Depends(get_db),
):
    """Mark a user's email as verified, without code."""
    try:
        return services.verify_email(db, user_id)
    except UserNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))


@router.delete(
    "/users/{user_id}/drop",
    status_code=204,
    responses={
        204: {"description": "Drop of the day deleted"},
        404: {"description": "No user with this id, or no drop of the day"},
    },
)
def delete_drop(
    user_id: int = Path(description="meloDrop user id (id in GET /admin/users)"),
    db: Session = Depends(get_db),
):
    """Delete a user's drop of the day."""
    try:
        services.delete_active_drop(db, user_id)
    except (UserNotFoundError, DropNotFoundError) as e:
        raise HTTPException(status_code=404, detail=str(e))


@router.delete(
    "/users/{user_id}",
    status_code=204,
    responses={
        204: {"description": "Account and all its data deleted"},
        400: {"description": "The typed username does not match this account"},
        403: {"description": "The admin can't delete their own account from here"},
        404: {"description": "No user with this id"},
    },
)
def delete_account(
    body: schemas.AdminDeleteConfirm,
    user_id: int = Path(description="meloDrop user id (id in GET /admin/users)"),
    admin: User = Depends(current_admin),
    db: Session = Depends(get_db),
):
    """Delete a user's account, after checking the typed username."""
    try:
        services.delete_account(db, admin, user_id, body.username)
    except UserNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except OwnAccountError as e:
        raise HTTPException(status_code=403, detail=str(e))
    except UsernameMismatchError as e:
        raise HTTPException(status_code=400, detail=str(e))