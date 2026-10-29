# Changelog

All notable changes to Melodrop are documented in this file.

## [0.4.1] - 2026-10-29

### Changed
- "Mes skips" moved from "Mes likes" to the profile: a butter-yellow "Mes skips ›" link under the counters (tester feedback: a skip is not a like).
- The back arrow of "Mes skips" now returns to the profile.


## [0.4.0] - 2026-10-29

### Added
- "Mes skips" screen: the songs skipped in the last 7 days, with their preview, their author and whether their drop is still online.
- Heart button to rescue a skipped song into "Mes likes":
    - drop still online: the author gets a like;
    - drop expired: no like for the author, and the song shows "Récupéré dans les skips" in "Mes likes".
- "Mes skips ›" button at the top of "Mes likes".
- Backend: `GET /me/skips` and `POST /me/skips/{drop_id}/rescue` routes, new `liked_late` column in `liked_songs` (Alembic migration).
- Demo data: three skips on expired drops for the `litoxam` account.

### Changed
- "Mes likes" reloads quietly when it comes back on screen.
- Liking a new drop of a song already rescued late turns it into a normal like, with its author.


## [0.3.0] - 2026-10-28

### Added
- Reminder notification "Ton drop expire dans 15 min", sent 15 minutes before the daily drop expires, even when the app is closed.
- The app asks for the notification permission right after a drop (Android 13 and above).
- The reminder is cancelled when the user logs out or deletes their account.

### Changed
- Server logs show the username of each request.

### Fixed
- Song genres are shown in French (Deezer sent them in English).
- One-off script to refresh the genre of the songs already saved.


## [0.2.1] - 2026-10-27

### Added
- A drop without a cover shows a sad drop with "Pas d'image dispo !".

### Fixed
- Songs without a cover on Deezer no longer cause errors: the cover is now optional in the API and in the app.
- The search results use the `artist` field, like the rest of the API.


## [0.2.0] - 2026-10-27

### Added
- Audio player on "Ton drop du jour", to listen to the preview of your own drop.
- Like and Skip buttons under the Discover card, for those who prefer to tap rather than swipe.
- Show / hide password button on the login screen.

### Changed
- The sign-up screen reminds to check the spam folder for the confirmation code.


## [0.1.0] - 2026-10-26

First release of Melodrop, sent to the first testers.

### Added
- Account
    - Sign-up with a username, an email and a password (at least 8 characters, a digit and a special character).
    - Email confirmed with a 6-digit code sent by email.
    - Login with the username or the email.
    - Forgotten password: a code sent by email to choose a new one.
- Discover
    - Random feed of the drops of the day from the other users.
    - Swipe right to like a drop, left to skip it.
    - 30-second preview of each song.
    - Genre of the song, taken from its Deezer album.
    - Tap on the author to open their profile.
    - "Plus de drop à découvrir" message once everything has been seen.
- Drop
    - Search for a song on Deezer by title or artist.
    - One drop at a time, online for 24 hours from the moment it is dropped.
    - "Ton drop du jour": the current drop, its preview, its likes and the time left.
- Mes likes
    - The liked songs, with their preview and their author.
    - Remove a song from the list.
- Profile
    - Counters: drops, likes received, songs liked.
    - Last drops, with the drop of the day marked "EN LIGNE".
    - Public display name and avatar (pastel color and drop style).
    - Profile of the other users.
- Account settings: change the email (confirmed with a code sent to the new address), change the password, log out, delete the account.
- Visual identity: dark theme with pastel colors, light halos that move between the tabs, water ripple animation on the login screen.
- Backend: FastAPI and MySQL, hosted on Scaleway at api.melodrop-app.com, database schema managed with Alembic migrations.