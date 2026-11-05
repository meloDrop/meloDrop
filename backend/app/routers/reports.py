"""Routes of the reports module: a user reports a profile from the app."""

from fastapi import APIRouter, Depends, HTTPException, Path
from sqlalchemy.orm import Session

from app.database import get_db
from app.dependencies import current_user
from app.exceptions import AlreadyReportedError, CannotReportSelfError, UserNotFoundError
from app.models.user import User
from app.schemas.reports import ReportCreate, ReportSentOut
from app.services import reports as services

router = APIRouter(prefix="/users", tags=["reports"])


@router.post(
    "/{user_id}/report",
    status_code=201,
    response_model=ReportSentOut,
    responses={
        201: {"description": "Report saved: it waits in the dashboard"},
        400: {"description": "A user can't report their own profile"},
        404: {"description": "No user has this id"},
        409: {"description": "The user already reported this profile, and it is not handled yet"},
        422: {"description": "Unknown reason, message too long, or no message for \"other\""},
    },
)
def report_user(
    body: ReportCreate,
    user_id: int = Path(description="meloDrop id of the user to report"),
    user: User = Depends(current_user),
    db: Session = Depends(get_db),
):
    """Report a profile: name, drop or Mixtape not OK, spam, harassment..."""
    try:
        return services.report_user(db, user.id, user_id, body)
    except UserNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except CannotReportSelfError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except AlreadyReportedError as e:
        raise HTTPException(status_code=409, detail=str(e))
