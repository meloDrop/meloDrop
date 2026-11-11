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

class DuplicateSongError(Exception):
    """Raised when the same song is given twice for the podium."""

class LabeledSongNotFoundError(Exception):
    """Raised when there is no song under this label on the user's Mixtape."""

class CannotFollowSelfError(Exception):
    """Raised when a user tries to follow their own account."""

class CannotReportSelfError(Exception):
    """Raised when a user tries to report their own profile."""

class AlreadyReportedError(Exception):
    """Raised when the user already reported this profile and the report is still open."""

# Admin actions

class OwnAccountError(Exception):
    """Raised when the admin tries to delete their own account from the admin routes."""

class UsernameMismatchError(Exception):
    """Raised when the typed username does not match the account to delete."""

class CoverNotAllowedError(Exception):
    """Raised when a cover link does not point to the Deezer image servers."""

class CoverNotFoundError(Exception):
    """Raised when a Deezer cover can't be downloaded, or is not a small image."""

# Badges, created from the dashboard

class BadgeNotFoundError(Exception):
    """Raised when no badge has this id."""

class BadgeNameTakenError(Exception):
    """Raised when another badge already has this name."""

class BadgeAlreadyAwardedError(Exception):
    """Raised when the user already has this badge."""

class UserBadgeNotFoundError(Exception):
    """Raised when the user does not have this badge."""

# Reports, sent from the app and handled from the dashboard

class ReportNotFoundError(Exception):
    """Raised when no report has this id."""

# Cosmetics of the avatar

class PremiumRequiredError(Exception):
    """Raised when a user without the subscription picks a premium cosmetic."""

