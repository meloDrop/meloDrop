"""Sending emails through the Brevo SMTP relay (ADR-0016)."""

import smtplib
import ssl
from datetime import datetime
from email.message import EmailMessage
from email.utils import format_datetime
from zoneinfo import ZoneInfo

from app.config import settings

# Time zone shown in the date of the emails (the database stays in UTC)
MAIL_TIMEZONE = ZoneInfo("Europe/Paris")


def send_email(to: str, subject: str, body: str) -> None:
    """Send a plain text email."""
    # Build the email: sender, receiver, date, subject and text
    message = EmailMessage()
    message["From"] = settings.MAIL_FROM
    message["To"] = to
    # Paris time with its offset: +0200 in summer, +0100 in winter
    message["Date"] = format_datetime(datetime.now(MAIL_TIMEZONE))
    message["Subject"] = subject
    message.set_content(body)

    # Connect to Brevo, encrypt the connection, log in, then send
    with smtplib.SMTP(settings.SMTP_HOST, settings.SMTP_PORT, timeout=10) as server:
        server.starttls(context=ssl.create_default_context())
        server.login(settings.SMTP_USER, settings.SMTP_PASSWORD)
        server.send_message(message)