"""Errors raised by the services."""

# Authentication to the app

class UserAlreadyExistsError(Exception):
    """Raised when the email or username is already taken."""

class InvalidCredentialsError(Exception):
    """Raised when the identifier or the password is wrong."""

class InvalidPasswordError(Exception):
    """Raised when a new password doesn't meet the complexity rules."""

# Use of the Deezer public API

class DeezerError(Exception):
    """Raised when Deezer returns an error or does not answer."""

class TrackNotFoundError(DeezerError):
    """Raised when Deezer has no track with this id."""

class SongNotFoundError(Exception):
    """Raised when no song with this id is stored in meloDrop."""

class DropAlreadyActiveError(Exception):
    """Raised when the user already has a drop that has not expired."""

class DropNotFoundError(Exception):
    """Raised when a drop does not exist or has expired."""


class OwnDropError(Exception):
    """Raised when a user tries to save or skip their own drop."""


class DropAlreadySeenError(Exception):
    """Raised when a user has already saved or skipped this drop."""

class InvalidCodeError(Exception):
    """Raised when an email code is wrong, expired or tried too many times."""

class EmailNotVerifiedError(Exception):
    """Raised when a user tries to log in before confirming their email."""

class LikeNotFoundError(Exception):
    """Raised when a song is not in the user's "Mes likes" list."""

class SkipNotFoundError(Exception):
    """Raised when the user has not skipped this drop."""

class UserNotFoundError(Exception):
    """Raised when no user has this id."""