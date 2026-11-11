# Changelog

All notable changes to Melodrop are documented in this file.


## [0.10.2] - 2026-11-11

### Fixed
- Google Play: the portrait mode of 0.10.1 made the app incompatible with 25 devices, the ones with no portrait screen. The portrait screen is now declared as optional (`uses-feature` with `required="false"`), so these devices keep the app. Phones still stay in portrait.


## [0.10.1] - 2026-11-11

### Changed
- The app stays in portrait: it no longer turns sideways on a phone. Android 16 and later still turn it on big screens (tablets, unfolded foldables).
- The texts of the "Nouveautés" sheet present version 0.10.1.

### Fixed
- Discover: with a long @username, the line "Déposé par" took the whole width of the card. The likes had no room left and grew very tall, one letter per line, which pushed the cover and the play button out of the card. The likes now get their width first, and the @username shows under "Déposé par", always in full (on two lines for the longest ones on a narrow phone).
- Short screens: since the avatar of the dropper, the cover of the Discover card got very small. The avatar is smaller (24 dp), the song and its artist are closer, and on a short card (small phone) the padding and the spacing are smaller and the song stays on one line, cut with "…". The cover gets back at least the room it had before 0.10.0. The play button and the chips now go in the corners of the whole cover area: on a short screen they sit on the blurred sides and no longer hide the cover. A small cover gets smaller round corners, so it stays a square instead of a circle.
- Profiles: a long name was cut with "…". It now shows in full: on one line, a bit smaller when needed (down to 16 sp), then on several lines, a long word cut between two syllables with a "-".


## [0.10.0] - 2026-11-11

### Added
- Animations in Discover. A pick sends big waves from the card, a star, stars, the word "PICK" and a yellow flash. A like sends two waves from its button, a heart and hearts. A skip sends a grey wave and streaks to the left. The next card comes forward with a small bounce, and the empty feed shows slow waves.
- A rain of pastel drops on the Drop screen right after a drop. A big drop with the stripes of the logo falls last, and the drop of the day appears with its waves.
- "Déjà dans tes likes" on a Discover card when the song is already in "Mes likes".
- A short message under the buttons of Discover after each choice: "Ajouté à Mes likes", "Ajouté à Mes picks", "Passé, à retrouver dans Mes skips", or "Déjà dans tes likes : like compté".
- Avatar cosmetics: 11 drop colors (Nuit, Crème, the 5 pastels, and the premium Océan, Prune, Or and Spectre), 4 background decorations, 6 faces, two new styles (Jazz with a vintage microphone, Lo-fi with a beanie), and 9 animated auras shown around the avatar on the profiles, in the user lists (search, followers, likes, droppers) and on the Discover cards. The edit screen shows each choice on the user's own avatar, with a lock on the premium ones when they are locked.
- The avatar of the dropper on the Discover card, with its aura, next to "Déposé par". With several droppers, up to 3 small avatars show next to the line.
- Subscriber status: given or taken back from the dashboard ("Abonnement" in the account dialog, "abonné" in the user list). A subscriber gets the premium drops and the auras.
- Backend: `avatar_drop`, `avatar_deco`, `avatar_face` and `avatar_aura` in the profile, the user lists and `PATCH /me/profile`; `is_subscriber` and `premium_unlocked` in `GET /me`; `PATCH /admin/users/{user_id}/subscriber`; `PREMIUM_LOCKED` setting (false during the test phase); 5 columns in `users` (Alembic migration). A new premium choice gets a 403 when the cosmetics are locked and the user has no subscription; a premium choice already saved is kept.

### Changed
- The avatar is more detailed: the drop has volume, reflections and a soft shadow, the light drops have an outline, and every accessory has more details and a dark outline.
- New edit profile screen: a bigger avatar at the top with its aura, the name edited with a pencil, and the choices in 5 tabs (Style, Couleur, Fond, Visage, Aura). Only the choices of the open tab scroll, and the save button stays at the bottom.
- A song in "Mes likes" comes back in Discover when someone drops it again, with the same chance as the other cards. It stays hidden while one of the drops the user liked it on is still online. A new like counts only for the drops not liked yet, and the song is not added twice to "Mes likes".
- After a first drop, the notification window of Android waits for the end of the rain.
- The texts of the "Nouveautés" sheet present version 0.10.0.
- The feed view of the admin dashboard follows the new rule.


## [0.9.1] - 2026-11-08

### Changed
- The texts of the "Nouveautés" sheet present version 0.9.1.

### Fixed
- Tutorial: the bubble could cover the highlighted element, or go past the bottom of the screen and hide the "Suivant" button. It now goes on the side of the element with the most room, inside the status and navigation bars, and never gets taller than that room: its text scrolls, the buttons stay visible. It is 480 dp wide at most on a tablet or a phone held sideways, and the page scrolls to the element of the step once it is laid out.
- Short screens: the cover of the Discover card left empty bands on its sides. The same cover, enlarged, blurred (Android 12 and later) and darkened, now fills them.


## [0.9.0] - 2026-11-08

### Added
- One card per song in Discover (ADR-0021): when several members drop the same song, it shows once with "X personnes ont droppé cette chanson", and a like or a pick counts for every dropper. A tap on this line, or on the likes, opens who dropped the song and who liked it (a pick shows as a like there).
- Who liked and who picked my drop: from the likes and picks tiles of the Drop screen, and from the stats under the user's drops (profile and "Mes drops"), in two tabs.
- "Voir plus" and a drops counter that can be tapped on the profile of the others: "Ses drops" lists all their drops.
- "drop par X personnes" in "Mes likes", "Mes picks" and "Mes skips", which opens the list of the droppers. The search of "Mes likes" and "Mes picks" looks in every dropper.
- Tutorials on example screens the first time Discover, Drop and Profil open on a phone, and after a new account: the swipes are shown on an example card. "Revoir le tuto" in the "Nouveautés" sheet shows the three in a row.
- Automatic badges: a badge can have a rule and a threshold, and is given by itself when a member reaches it (admin dashboard).
- Backend: `GET /drops/{drop_id}/likers` and `GET /songs/{song_id}/likers` routes; `droppers` in `GET /feed`, `GET /me/likes`, `GET /me/picks` and `GET /me/skips`; `credited_count` in the rescue of a skip; `rule` and `threshold` columns in `badges` (Alembic migration).

### Changed
- The feed draws the songs, not the drops: a song dropped by many members does not come first. Songs already in "Mes likes", skipped in the last 7 days, or dropped by the user and still online are not shown.
- A swipe, and a rescue from "Mes skips", count for every drop of the song still online. "Mes skips" shows a song once.
- The "Nouveautés" sheet waits for the end of a tutorial. Its texts present version 0.9.0.
- The feed view of the admin dashboard follows the same rules as the app.

### Fixed
- Short screens: the one-line hint of Discover follows the order of the buttons (Passer, Pick, Liker).


## [0.8.0] - 2026-11-05

### Added
- "Signaler le profil" in the "⋮" menu of the profile of the others: a reason (name, drop or Mixtape, spam, harassment, other) and a message, required for "other". The meloDrop team handles it from the admin dashboard.
- Search and sort in "Mes likes" and "Mes picks": search by title, artist, genre or @dropper, sort by date, genre or dropper, with a small title for each group.
- Views of a drop: a view is a swipe (like, pick or skip). The Drop screen shows the likes, the picks, the views and the like rate of the drop of the day, and the drops of the user (profile and "Mes drops") show their views and their like rate.
- Admin dashboard: Signalements tab with a red counter (remove the name and the avatar, delete the drop, delete the account, close with a note), "Donner à plusieurs" for the badges, likes and picks apart everywhere, views and like rate of the drops, Abonnements card, profile of a user like in the app (Mixtape and Activité), "Les picks" and "Le podium surprise" in the Récap.
- Backend: `POST /users/{user_id}/report`, `GET /admin/reports`, `PATCH /admin/reports/{report_id}` and `POST /admin/badges/{badge_id}/award` routes; new `reports` table and `is_pick` column in `saves` (Alembic migrations, the old saves are filled from the picks of "Mes likes"); `picks_count` and `views_count` in `GET /drops/me` and in the drops of `GET /users/{id}/profile`; `genre` in `GET /me/likes` and `GET /me/picks`.

### Changed
- "drop par @…" under a pick opens the profile of the dropper, on the profile and on the "Mes picks" screen.
- The texts of the "Nouveautés" sheet present version 0.8.0.

### Fixed
- Small screens: the "ALL TIME" badge of the podium was written letter by letter, in a column. It keeps its width, and the title goes on two lines when needed.
- Small screens: the cover of Discover became tiny, the play button covered it and the genre was cut. The "Abonnements d'abord" chip takes less room, the hint fits on one line, and on a small cover the play button gets smaller and the genre and "Abonné" chips are hidden.
- Profile of the others: the "Suivre en retour" button squeezed the follow counters on narrow screens. It moves to its own line, full width, under the counters.


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