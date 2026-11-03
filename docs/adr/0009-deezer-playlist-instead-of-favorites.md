# ADR-0009: Save to a meloDrop Deezer Playlist Instead of Favorites

**Date:** 2026-10-05
**Status:** Superseded by ADR-0010

## Context

The original plan was to add a saved track directly to the user's Deezer favorites (`POST /user/me/tracks`). Favorites are simple but mix meloDrop saves with the user's existing library, with no visible trace of meloDrop on Deezer itself.

## Decision

A save now goes into a dedicated "meloDrop" playlist on Deezer instead of the favorites. The playlist is created once, during the Deezer OAuth callback, and its id is stored in `platform_links.deezer_playlist_id`. If the playlist no longer exists on Deezer (the user deleted it), the provider recreates it on the next save and the new id replaces the old one.

## Consequences

**Positive:**
- The meloDrop playlist is visible and identifiable on the user's Deezer account, reinforcing the brand.
- No extra OAuth permission needed: playlist creation and track addition both use `manage_library`, already requested.

**Trade-offs:**
- Adds a `deezer_playlist_id` column to `platform_links` and a migration step in `schema.sql`.
- One more failure mode to handle: a 404 on adding a track means the playlist was deleted externally and must be recreated before retrying.