"""Errors raised by the services."""


class UserAlreadyExistsError(Exception):
    """Raised when the email or username is already taken."""


class InvalidCredentialsError(Exception):
    """Raised when the identifier or the password is wrong."""