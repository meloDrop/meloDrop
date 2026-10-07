# ADR-0012: V1 Likes in a Single List

**Date:** 2026-10-07
**Status:** Accepted

## Context

ADR-0011 planned several playlists per user, with a playlist picker at each right swipe, renaming and deletion. This adds about six routes and two screens, while the backend must be finished around October 20 so the Android app can be built before the end of October. User playlists are also a good candidate for a future paid version.

## Decision

V1 keeps the choices of ADR-0011 (no streaming account linking, public Deezer API) but replaces user playlists with a single "Mes likes" list per user, filled by right swipes. Creating playlists moves to V2, possibly as a paid feature, and ADR-0011 is superseded.

## Consequences

**Positive:**
- A like is a single swipe, with no playlist to choose.
- V2 can add playlists on top of the likes list without changing it.

**Trade-offs:**
- Users cannot sort their discoveries until V2.
- All liked tracks end up in one list, which can grow long.