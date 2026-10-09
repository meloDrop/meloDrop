"""Errors raised by the services."""

# Authentication to the app

class UserAlreadyExistsError(Exception):
    """Raised when the email or username is already taken."""

class InvalidCredentialsError(Exception):
    """Raised when the identifier or the password is wrong."""

class InvalidPasswordError(Exception):
    """Raised when a new password doesn't meet the complexity rules."""

# Authentication to a Streaming Plateform (v2)
class OAuthDeniedError(Exception):
    """Raised when the user refuses to authorize the app on the streaming platform."""

class AccountAlreadyLinkedError(Exception):
    """Raised when a streaming account is already linked to another meloDrop user."""


# Use of the Deezer public API

class DeezerError(Exception):
    """Raised when Deezer returns an error or does not answer."""

class TrackNotFoundError(DeezerError):
    """Raised when Deezer has no track with this id."""

class SongNotFoundError(Exception):
    """Raised when no song with this id is stored in meloDrop."""

class DropAlreadyActiveError(Exception):
    """Raised when the user already has a drop that has not expired."""

