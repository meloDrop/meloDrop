"""Functions that save the reports sent from the app, and handle them from the dashboard."""

from datetime import timezone

from sqlalchemy.orm import Session

from app.exceptions import (
    AlreadyReportedError,
    CannotReportSelfError,
    ReportNotFoundError,
    UserNotFoundError,
)
from app.models.report import Report
from app.models.user import User
from app.schemas.reports import (
    AdminReportOut,
    ReportCreate,
    ReportSentOut,
    ReportUpdate,
    ReportUserOut,
)
from app.utils.time import utc_now


def report_user(db: Session, reporter_id: int, reported_id: int, data: ReportCreate) -> ReportSentOut:
    """Save a report on a profile. It waits in the Signalements tab of the dashboard."""
    # The reported user must exist
    reported = db.get(User, reported_id)
    if reported is None:
        raise UserNotFoundError("user_not_found")

    if reporter_id == reported_id:
        raise CannotReportSelfError("cannot_report_self")

    # One open report per user and per profile: the admin already has it
    already = (
        db.query(Report)
        .filter(
            Report.reporter_id == reporter_id,
            Report.reported_id == reported_id,
            Report.status == "open",
        )
        .first()
    )
    if already is not None:
        raise AlreadyReportedError("already_reported")

    report = Report(
        reporter_id=reporter_id,
        reported_id=reported_id,
        reported_username=reported.username,
        reason=data.reason,
        message=data.message,
    )
    db.add(report)
    db.commit()
    db.refresh(report)
    return ReportSentOut(id=report.id, created_at=report.created_at.replace(tzinfo=timezone.utc))


def public_user(user: User | None) -> ReportUserOut | None:
    """A user of a report, without private data. None once the account is deleted."""
    if user is None:
        return None
    return ReportUserOut(
        id=user.id,
        username=user.username,
        display_name=user.display_name,
        avatar_color=user.avatar_color,
        avatar_style=user.avatar_style,
    )


def build_report(db: Session, report: Report) -> AdminReportOut:
    """Build a report for the dashboard, with the two users."""
    reporter = db.get(User, report.reporter_id) if report.reporter_id else None
    reported = db.get(User, report.reported_id) if report.reported_id else None
    closed_at = report.closed_at.replace(tzinfo=timezone.utc) if report.closed_at else None
    return AdminReportOut(
        id=report.id,
        status=report.status,
        reason=report.reason,
        message=report.message,
        note=report.note,
        created_at=report.created_at.replace(tzinfo=timezone.utc),
        closed_at=closed_at,
        reporter=public_user(reporter),
        reported=public_user(reported),
        reported_username=report.reported_username,
    )


def list_reports(db: Session) -> list[AdminReportOut]:
    """Return every report, the newest first. The page sorts them by status."""
    reports = db.query(Report).order_by(Report.created_at.desc(), Report.id.desc()).all()

    all_reports = []
    for report in reports:
        all_reports.append(build_report(db, report))
    return all_reports


def count_open_reports(db: Session) -> int:
    """Number of reports still to handle, for the overview of the dashboard."""
    return db.query(Report).filter(Report.status == "open").count()


def update_report(db: Session, report_id: int, changes: ReportUpdate) -> AdminReportOut:
    """Close a report (resolved or dismissed) with a note, or open it again."""
    report = db.get(Report, report_id)
    if report is None:
        raise ReportNotFoundError("report_not_found")

    # The closing date is kept while the report stays closed
    if changes.status == "open":
        report.closed_at = None
    elif report.status == "open":
        report.closed_at = utc_now()
    report.status = changes.status

    # None: note not sent, it stays. Empty text: the note is removed
    if changes.note is not None:
        report.note = changes.note or None

    db.commit()
    return build_report(db, report)
