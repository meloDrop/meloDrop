"""Errors raised by the services."""


class UserAlreadyExistsError(Exception):
    """Raised when the email or username is already taken."""


class InvalidCredentialsError(Exception):
    """Raised when the identifier or the password is wrong."""

class InvalidPasswordError(Exception):
    """Raised when a new password doesn't meet the complexity rules."""

class OAuthDeniedError(Exception):
    """Raised when the user refuses to authorize the app on the streaming platform."""