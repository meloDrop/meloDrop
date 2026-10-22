# ADR-0020: Avatar Built From Closed Lists

**Date:** 2026-10-22
**Status:** Accepted

## Context

Each profile needs a picture that makes users easy to recognize. A photo upload would need file storage, size checks and image moderation, and the app has no moderation since ADR-0017. The MVP is due on October 31.

## Decision

The avatar is a drop drawn by the app, on one of 5 pastel backgrounds and in one of 7 drop styles (simple, rock, rap, pop, classical, reggae, electro). The two choices are stored as short names in `users.avatar_color` and `users.avatar_style`, and `PATCH /me/profile` only accepts names from these lists.

## Consequences

**Positive:**
- No files to store or moderate: an avatar is two short words in the database.
- Every avatar matches the look of the app.

**Trade-offs:**
- Less personal than a photo.
- The two lists live in the backend and in `Avatar.kt`, and must be changed together. An app that does not know a style yet shows the simple drop.