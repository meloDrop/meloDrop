# Changelog

All notable changes to Melodrop are documented in this file.


## [0.7.0] - 2026-11-05

### Added
- Profile on two pages you swipe between: Activité (counters, drops, picks) and Mixtape. Your own profile opens on Activité, the profile of the others on the Mixtape.
- Podium of the Mixtape: your 3 favorite songs of all time, with their preview, chosen from the "Mon podium" screen.
- Labeled songs on the Mixtape: one song for each of the 8 labels, like "Le son qui me fait chialer" or "Mon générique préféré", chosen from the "Mes chansons" screen.
- Badges: created and given from the admin dashboard, shown on the Mixtape, 4 per line. A tap opens their description and the day they were received.
- Song poster: a tap on a cover opens the album, the release date, the length, the genre, and the drops, likes and picks of the song on meloDrop. It opens from the Mixtape, the Activité page, "Mes likes", "Mes skips", "Mes drops" and "Mes picks".
- Follows: "Suivre" (or "Suivre en retour") button on the profile of the others, followers and following counters on every profile, and lists that open from these counters.
- "Abonnements d'abord" chip in Discover, on by default: the drops of the followed users come first, with "Abonné" on their card.
- Admin dashboard: Badges tab (create, edit, delete, with a preview) and Badges card on the page of a user (give, take back).
- Backend: `PUT /me/podium`, `PUT /me/labels/{label}`, `DELETE /me/labels/{label}`, `GET /songs/{song_id}`, `POST /users/{user_id}/follow`, `DELETE /users/{user_id}/follow`, `GET /users/{user_id}/followers`, `GET /users/{user_id}/following` and admin badge routes; new `podium_songs`, `labeled_songs`, `badges`, `user_badges` and `follows` tables (Alembic migrations); `GET /users/{id}/profile` returns the podium, the labeled songs, the badges and the follow counters; `GET /feed` takes a `follows_first` parameter.

### Changed
- In the drops and picks grids, a tap on the cover opens the song poster: only the small button plays the preview.
- The texts of the "Nouveautés" sheet present version 0.7.0.


## [0.6.0] - 2026-11-05

### Added
- Picks: a third choice in Discover, for a song you discover and love at first listen. Tap the yellow star or swipe the card up. A pick also counts as a like for the author of the drop.
- Star on each song of "Mes likes": it turns a like into a pick, and a second tap removes it from the picks.
- "Mes drops" and "Mes picks" tabs on your profile: the "Mes picks" tab shows your last 5 picks, and a "Tout voir" tile opens the "Mes picks" screen with all of them.
- "Nouveautés" sheet: it opens by itself once after each update, and the drop-shaped "i" button of Discover opens it again at any time.
- Backend: `POST /drops/{drop_id}/pick`, `GET /me/picks`, `POST /me/picks/{song_id}` and `DELETE /me/picks/{song_id}` routes, new `is_pick` column in `liked_songs` (Alembic migration), `is_pick` field in `GET /me/likes`.

### Changed
- The three buttons of Discover (skip, pick, like) have the same size, and the Pick button stands a little higher.
- The hint under the cards now says "Glisse à droite pour Liker, à gauche pour Passer", with a second line for the picks.
- "Mes skips" link of the profile in lavender: butter yellow is now the color of the picks.


## [0.5.0] - 2026-11-02

### Added
- User search: a magnifying glass at the top right of Discover, to find another user by the start of their @username and open their profile.
- Play button on every song of a profile (the drop of the day and the last drops), on your own profile and on the others'.
- "Mes drops" screen: all your drops, the most recent first, with their preview. It opens from the "drops" counter of the profile.
- Backend: `GET /users/search` and `GET /users/{user_id}/drops` routes.

### Changed
- The drop notification is now sent when the drop expires, with a new text: "Ton drop a expiré, dépose ta prochaine chanson !". It stays until the user taps or swipes it.
- Sign-up: the username can only contain letters without accents, digits, `_` and `-`. A hint under the field gives the rule, and the keyboard no longer adds accents.


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