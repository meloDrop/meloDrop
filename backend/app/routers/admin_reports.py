"""Routes of the reports, in the admin module: the Signalements tab of the dashboard."""

from fastapi import APIRouter, Depends, HTTPException, Path
from sqlalchemy.orm import Session

from app.database import get_db
from app.dependencies import current_admin
from app.exceptions import ReportNotFoundError
from app.schemas.reports import AdminReportOut, ReportUpdate
from app.services import reports as services

# Every route needs an admin: 401 if not logged in, 403 if not admin
router = APIRouter(prefix="/admin", tags=["admin"], dependencies=[Depends(current_admin)])


@router.get(
    "/reports",
    response_model=list[AdminReportOut],
    responses={200: {"description": "Every report, the newest first"}},
)
def list_reports(db: Session = Depends(get_db)):
    """List every report, for the Signalements tab of the dashboard."""
    return services.list_reports(db)


@router.patch(
    "/reports/{report_id}",
    response_model=AdminReportOut,
    responses={
        200: {"description": "Report closed or opened again"},
        404: {"description": "No report with this id"},
        422: {"description": "Unknown status, or note too long"},
    },
)
def update_report(
    body: ReportUpdate,
    report_id: int = Path(description="Report id (id in GET /admin/reports)"),
    db: Session = Depends(get_db),
):
    """Close a report as handled or without action, with a note, or open it again.

    The actions on the account (name, avatar, drop, account) use the routes of /admin/users.
    """
    try:
        return services.update_report(db, report_id, body)
    except ReportNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
