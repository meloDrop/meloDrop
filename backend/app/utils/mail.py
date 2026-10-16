"""Sending emails through the Brevo SMTP relay (ADR-0016)."""

import smtplib
import ssl
from datetime import datetime
from email.message import EmailMessage
from email.utils import format_datetime
from pathlib import Path
from zoneinfo import ZoneInfo

from jinja2 import Environment, FileSystemLoader

from app.config import settings

# Time zone shown in the date of the emails (the database stays in UTC)
MAIL_TIMEZONE = ZoneInfo("Europe/Paris")

# Email templates live in app/templates/email/
TEMPLATE_DIR = Path(__file__).resolve().parent.parent / "templates" / "email"

# autoescape protects the HTML if a variable contains special characters
templates = Environment(loader=FileSystemLoader(TEMPLATE_DIR), autoescape=True)

# Subject of each code email, by kind
CODE_SUBJECTS = {
    "verify_email": "Ton code de vérification meloDrop",
    "reset_password": "Réinitialisation de ton mot de passe meloDrop",
}


def send_email(to: str, subject: str, body: str, html: str | None = None) -> None:
    """Send an email, with an HTML version if one is given."""
    # Build the email: sender, receiver, date, subject and text
    message = EmailMessage()
    message["From"] = settings.MAIL_FROM
    message["To"] = to
    # Paris time with its offset: +0200 in summer, +0100 in winter
    message["Date"] = format_datetime(datetime.now(MAIL_TIMEZONE))
    message["Subject"] = subject
    message.set_content(body)
    # The mail app shows the HTML version, or the text one if it can't
    if html is not None:
        message.add_alternative(html, subtype="html")

    # Connect to Brevo, encrypt the connection, log in, then send
    with smtplib.SMTP(settings.SMTP_HOST, settings.SMTP_PORT, timeout=10) as server:
        server.starttls(context=ssl.create_default_context())
        server.login(settings.SMTP_USER, settings.SMTP_PASSWORD)
        server.send_message(message)


def send_code_email(to: str, kind: str, code: str, expires_minutes: int) -> None:
    """Send a 6-digit code email (kind: verify_email or reset_password)."""
    variables = {
        "kind": kind,
        "code": code,
        "expires_minutes": expires_minutes,
        "logo_src": settings.MAIL_LOGO_URL,
    }
    body = templates.get_template("code_email.txt").render(variables)
    html = templates.get_template("code_email.html").render(variables)
    send_email(to, CODE_SUBJECTS[kind], body, html)