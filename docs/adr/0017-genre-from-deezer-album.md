# ADR-0017: Genre From the Deezer Album

**Date:** 2026-10-20
**Status:** Accepted

## Context

Until now, the author of a drop picks its genres, and other users can report a wrong genre, which needs a moderation module (reports, threshold, correction by the author). Since the Discover genre filter was removed, the genre is only informative, and the MVP deadline is close. The Deezer track endpoint has no genre, but the album endpoint has one.

## Decision

The genre is read from the album of the track (`GET /album/{id}`) when the song is created, and stored in a nullable `songs.genre` column. User-picked genres, genre reports and the moderation module are removed.

## Consequences

**Positive:**
- No moderation module to build before the deadline: no report route and no "reported genre" screen.
- Dropping a song is simpler: search, then drop, with no genre step.
- Simpler schema: the `genres`, `drop_genres` and `genre_reports` tables are removed.

**Trade-offs:**
- The genre comes from the album, not the track: a song from a compilation or a soundtrack can get a wrong genre, and it applies to every drop of that song.
- One more Deezer call when a song is created. If it fails or the album has no genre, the column stays empty.
- Deezer's genre names replace the app's own list of 13 genres.