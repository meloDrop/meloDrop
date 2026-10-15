"""Sending emails through the Brevo SMTP relay (ADR-0016)."""

import smtplib
import ssl
from email.message import EmailMessage

from app.config import settings


def send_email(to: str, subject: str, body: str) -> None:
    """Send a plain text email."""
    # Build the email: sender, receiver, subject and text
    message = EmailMessage()
    message["From"] = settings.MAIL_FROM
    message["To"] = to
    message["Subject"] = subject
    message.set_content(body)

    # Connect to Brevo, encrypt the connection, log in, then send
    with smtplib.SMTP(settings.SMTP_HOST, settings.SMTP_PORT, timeout=10) as server:
        server.starttls(context=ssl.create_default_context())
        server.login(settings.SMTP_USER, settings.SMTP_PASSWORD)
        server.send_message(message)