"""Routes of the badges, in the admin module: create them, then give them to the users."""

from fastapi import APIRouter, Depends, HTTPException, Path
from sqlalchemy.orm import Session

from app.database import get_db
from app.dependencies import current_admin
from app.exceptions import (
    BadgeAlreadyAwardedError,
    BadgeNameTakenError,
    BadgeNotFoundError,
    UserBadgeNotFoundError,
    UserNotFoundError,
)
from app.schemas.badges import BadgeCreate, BadgeOut, BadgeUpdate, UserBadgeOut
from app.services import badges as services

# Every route needs an admin: 401 if not logged in, 403 if not admin
router = APIRouter(prefix="/admin", tags=["admin"], dependencies=[Depends(current_admin)])


@router.get(
    "/badges",
    response_model=list[BadgeOut],
    responses={200: {"description": "Every badge, the newest first, with the users who have it"}},
)
def list_badges(db: Session = Depends(get_db)):
    """List every badge, for the Badges tab of the dashboard."""
    return services.list_badges(db)


@router.post(
    "/badges",
    status_code=201,
    response_model=BadgeOut,
    responses={
        201: {"description": "Badge created, nobody has it yet"},
        409: {"description": "Another badge already has this name"},
        422: {"description": "Empty or too long text, unknown icon or color"},
    },
)
def create_badge(body: BadgeCreate, db: Session = Depends(get_db)):
    """Create a badge, to give it to users afterwards."""
    try:
        return services.create_badge(db, body)
    except BadgeNameTakenError as e:
        raise HTTPException(status_code=409, detail=str(e))


@router.patch(
    "/badges/{badge_id}",
    response_model=BadgeOut,
    responses={
        200: {"description": "Badge changed, on every profile that has it"},
        404: {"description": "No badge with this id"},
        409: {"description": "Another badge already has this name"},
        422: {"description": "Empty or too long text, unknown icon or color"},
    },
)
def update_badge(
    body: BadgeUpdate,
    badge_id: int = Path(description="Badge id (id in GET /admin/badges)"),
    db: Session = Depends(get_db),
):
    """Change the name, the description, the icon or the color of a badge."""
    try:
        return services.update_badge(db, badge_id, body)
    except BadgeNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except BadgeNameTakenError as e:
        raise HTTPException(status_code=409, detail=str(e))


@router.delete(
    "/badges/{badge_id}",
    status_code=204,
    responses={
        204: {"description": "Badge deleted, and taken back from everyone"},
        404: {"description": "No badge with this id"},
    },
)
def delete_badge(
    badge_id: int = Path(description="Badge id (id in GET /admin/badges)"),
    db: Session = Depends(get_db),
):
    """Delete a badge: it disappears from every profile."""
    try:
        services.delete_badge(db, badge_id)
    except BadgeNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))


@router.get(
    "/users/{user_id}/badges",
    response_model=list[UserBadgeOut],
    responses={
        200: {"description": "The badges of the user, the most recently given first"},
        404: {"description": "No user with this id"},
    },
)
def list_user_badges(
    user_id: int = Path(description="meloDrop user id (id in GET /admin/users)"),
    db: Session = Depends(get_db),
):
    """List the badges of one user."""
    try:
        return services.list_user_badges(db, user_id)
    except UserNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))


@router.post(
    "/users/{user_id}/badges/{badge_id}",
    status_code=201,
    response_model=list[UserBadgeOut],
    responses={
        201: {"description": "Badge given: all the badges of the user"},
        404: {"description": "No user or no badge with this id"},
        409: {"description": "The user already has this badge"},
    },
)
def award_badge(
    user_id: int = Path(description="meloDrop user id (id in GET /admin/users)"),
    badge_id: int = Path(description="Badge id (id in GET /admin/badges)"),
    db: Session = Depends(get_db),
):
    """Give a badge to a user: it shows on their Mixtape."""
    try:
        return services.award_badge(db, user_id, badge_id)
    except (UserNotFoundError, BadgeNotFoundError) as e:
        raise HTTPException(status_code=404, detail=str(e))
    except BadgeAlreadyAwardedError as e:
        raise HTTPException(status_code=409, detail=str(e))


@router.delete(
    "/users/{user_id}/badges/{badge_id}",
    status_code=204,
    responses={
        204: {"description": "Badge taken back"},
        404: {"description": "No user with this id, or the user does not have this badge"},
    },
)
def revoke_badge(
    user_id: int = Path(description="meloDrop user id (id in GET /admin/users)"),
    badge_id: int = Path(description="Badge id (id in GET /admin/badges)"),
    db: Session = Depends(get_db),
):
    """Take a badge back from a user. The badge stays for the others."""
    try:
        services.revoke_badge(db, user_id, badge_id)
    except (UserNotFoundError, UserBadgeNotFoundError) as e:
        raise HTTPException(status_code=404, detail=str(e))
