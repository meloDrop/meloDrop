# ADR-0018: One Profile Route for Both Profiles

**Date:** 2026-10-22
**Status:** Accepted

## Context

The Stage 3 plan has three routes for profiles: `GET /me/stats`, `GET /users/{id}/stats` and `GET /users/{id}/profile`. The "Mon profil" mockup shows the user's latest drops, but none of these routes returns them for the logged-in user. The MVP deadline is close, and every route must be written and tested.

## Decision

Both profile screens use `GET /users/{id}/profile`, which returns the username, the sign-up date, the three counters, the drop of the day and the 5 last drops. `GET /me/stats` and `GET /users/{id}/stats` are dropped: the stats module only keeps its `get_stats` service, used by the profile.

## Consequences

**Positive:**
- One route to write and test instead of three.
- Both profiles show the same data in the same format, and the user's own profile gets its latest drops with no extra route.

**Trade-offs:**
- The user's own profile needs two calls: `GET /me` to get the user id, then the profile.
- The route list differs from the Stage 3 document, which is not updated.