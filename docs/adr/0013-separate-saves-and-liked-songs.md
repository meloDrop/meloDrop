# ADR-0013: Separate Saves and Liked Songs

**Date:** 2026-10-07
**Status:** Accepted

## Context

A right swipe adds the track to the user's "Mes likes" list, and the user can later remove a track from it. The `saves` table already records which drops a user liked: it keeps liked drops out of the Discover feed and counts the likes each author receives. Its rows are deleted with their drop, so a drop deleted along with its author's account would also remove the track from other users' likes.

## Decision

A new `liked_songs` table links users to songs, separate from `saves`, and a like writes one row in each table in the same transaction. The user's own likes counter is computed from `liked_songs`, and the likes received on their drops from `saves`.

## Consequences

**Positive:**
- Removing a track lowers the user's own likes counter, and the drop does not come back in the Discover feed.
- Liked tracks stay in the likes when a drop's author deletes their account, because `liked_songs` points to `songs`, which are never deleted.

**Trade-offs:**
- A like writes two rows, so both inserts must be committed together.
- When a user removes a track, the author's "likes received" counter does not go down, because `saves` keeps the original like.