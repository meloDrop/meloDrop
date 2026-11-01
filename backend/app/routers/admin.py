"""Routes of the admin module."""

from pathlib import Path as FilePath
from typing import Literal

from fastapi import APIRouter, Depends, HTTPException, Path, Query, Response
from fastapi.responses import FileResponse
from sqlalchemy.orm import Session

from app.database import get_db
from app.dependencies import current_admin
from app.exceptions import (
    CoverNotAllowedError,
    CoverNotFoundError,
    DeezerError,
    DropNotFoundError,
    OwnAccountError,
    TrackNotFoundError,
    UserAlreadyExistsError,
    UserNotFoundError,
    UsernameMismatchError,
)
from app.models.user import User
from app.schemas import admin as schemas
from app.schemas.profile import ProfileOut, ProfileUpdate
from app.services import admin as services
from app.services.admin_catalog import get_catalog_track, search_catalog
from app.services.admin_cover import get_cover
from app.services.admin_drops import get_drop_swipes, get_drops
from app.services.admin_overview import get_overview
from app.services.admin_recap import get_recap
from app.services.admin_songs import get_songs
from app.services.admin_user import get_user_detail

# Every route needs an admin: 401 if not logged in, 403 if not admin
router = APIRouter(prefix="/admin", tags=["admin"], dependencies=[Depends(current_admin)])

page_router = APIRouter(prefix="/admin", include_in_schema=False)

ADMIN_PAGE_DIR = FilePath(__file__).resolve().parent.parent / "static" / "admin"

PAGE_HEADERS = {
    "Content-Security-Policy": (
        "default-src 'self'; "
        "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; "
        "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; "
        "font-src https://fonts.gstatic.com; "
        "img-src 'self' data: blob: https://*.dzcdn.net; "
        "media-src https://*.dzcdn.net; "
        "connect-src 'self'; "
        "frame-ancestors 'none'; base-uri 'none'; form-action 'self'"
    ),
    "Referrer-Policy": "no-referrer",
    "Cache-Control": "no-store",
}


@page_router.get("")
def admin_page():
    """The admin dashboard: a login screen, then the tabs."""
    return FileResponse(ADMIN_PAGE_DIR / "index.html", headers=PAGE_HEADERS)


@page_router.get("/login-background.png")
def login_background():
    """Background image of the login screen, the same as in the app."""
    return FileResponse(ADMIN_PAGE_DIR / "login-background.png")
@router.get(
    "/users",
    response_model=list[schemas.AdminUserOut],
    responses={200: {"description": "Every account, the newest first"}},
)
def list_users(db: Session = Depends(get_db)):
    """List every account, to find the id of the one to manage."""
    return services.list_users(db)

@router.get(
    "/overview",
    responses={200: {"description": "Numbers of the overview tab of the dashboard"}},
)
def overview(db: Session = Depends(get_db)):
    """Counters, curves, activity, drops online and users, for the dashboard."""
    return get_overview(db)


@router.get(
    "/users/{user_id}",
    responses={
        200: {"description": "Everything about the user, for the Utilisateurs tab"},
        404: {"description": "No user with this id"},
    },
)
def user_detail(
    user_id: int = Path(description="meloDrop user id (id in GET /admin/users)"),
    db: Session = Depends(get_db),
):
    """Account, profile, counters, history and affinities of one user."""
    try:
        return get_user_detail(db, user_id)
    except UserNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))


@router.patch(
    "/users/{user_id}/profile",
    response_model=ProfileOut,
    responses={
        200: {"description": "Display name or avatar changed"},
        404: {"description": "No user with this id"},
    },
)
def update_user_profile(
    body: ProfileUpdate,
    user_id: int = Path(description="meloDrop user id (id in GET /admin/users)"),
    db: Session = Depends(get_db),
):
    """Change the display name or the avatar of a user, like PATCH /me/profile."""
    try:
        return services.update_user_profile(db, user_id, body)
    except UserNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))


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

@router.get(
    "/drops",
    responses={200: {"description": "Every drop, the newest first, for the Drops tab"}},
)
def drops(db: Session = Depends(get_db)):
    """Every drop with its song, its author, its likes and skips."""
    return get_drops(db)


@router.get(
    "/drops/{drop_id}/swipes",
    responses={200: {"description": "Who liked or skipped the drop (empty for an unknown drop)"}},
)
def drop_swipes(
    drop_id: int = Path(description="meloDrop drop id (drop_id in GET /admin/drops)"),
    db: Session = Depends(get_db),
):
    """Who liked or skipped one drop, the most recent first."""
    return get_drop_swipes(db, drop_id)


@router.get(
    "/songs",
    responses={200: {"description": "Every song stored in meloDrop, for the Sons tab"}},
)
def songs(db: Session = Depends(get_db)):
    """Every song with its drops, likes, skips and place in "Mes likes"."""
    return get_songs(db)


@router.get(
    "/catalog",
    responses={
        200: {"description": "Tracks found on Deezer, with those already in meloDrop"},
        502: {"description": "Deezer returned an error or did not answer"},
    },
)
def catalog_search(
    q: str = Query(min_length=1, max_length=100, description="Same search as the app"),
    db: Session = Depends(get_db),
):
    """Search tracks on Deezer, like the app, with the meloDrop counters."""
    try:
        return search_catalog(db, q)
    except DeezerError as e:
        raise HTTPException(status_code=502, detail=str(e))


@router.get(
    "/catalog/{track_id}",
    responses={
        200: {"description": "Everything about the Deezer track"},
        404: {"description": "No Deezer track with this id"},
        502: {"description": "Deezer returned an error or did not answer"},
    },
)
def catalog_track(
    track_id: int = Path(description="Deezer track id"),
    db: Session = Depends(get_db),
):
    """One Deezer track, and what meloDrop would store at its first drop."""
    try:
        return get_catalog_track(db, track_id)
    except TrackNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except DeezerError as e:
        raise HTTPException(status_code=502, detail=str(e))


@router.get(
    "/recap",
    responses={200: {"description": "Stats of the period, to share with the testers"}},
)
def recap(
    days: Literal["1", "7", "30", "all"] = Query("7", description="1, 7 or 30 days, or all since the start"),
    db: Session = Depends(get_db),
):
    """Podium, rankings and totals of a period, without any email."""
    return get_recap(db, None if days == "all" else int(days))


@router.get(
    "/cover",
    response_class=Response,
    responses={
        200: {"description": "The Deezer cover image", "content": {"image/jpeg": {}}},
        400: {"description": "Not a link to the Deezer image servers"},
        404: {"description": "Cover not found on Deezer, or too big"},
    },
)
def cover(url: str = Query(description="Deezer cover link (cover_url)")):
    """A Deezer cover sent through the API, for the image of the Récap card."""
    try:
        content_type, body = get_cover(url)
    except CoverNotAllowedError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except CoverNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
    return Response(content=body, media_type=content_type, headers={"Cache-Control": "private, max-age=86400"})