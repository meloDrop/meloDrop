# ADR-0010: Spotify Replaces Deezer as Reference Platform

**Date:** 2026-10-05
**Status:** Accepted

## Context

Deezer was the reference platform of the MVP for search, ISRC lookup, account linking and library additions. Deezer has suspended the creation of new developer apps with no reopening date, so no app ID or secret can be obtained and the OAuth flow can never succeed. Spotify still accepts new apps, but the app owner now needs an active Premium subscription, and an app in development mode is limited to five test accounts.

## Decision

Spotify replaces Deezer as the reference streaming platform of the MVP, and the Deezer code stays in the repository but is no longer used by the app. As in ADR-0009 (now superseded), saved tracks go to a dedicated meloDrop playlist, now created on Spotify.

## Consequences

**Positive:**
- The `StreamingProvider` interface absorbs the change: Spotify is added as a new provider file next to `deezer.py`.
- Spotify returns the ISRC of each track, so the matching between catalogs still works.
- Spotify tokens expire, so the automatic token refresh planned in Stage 1 is really implemented.

**Trade-offs:**
- The app only works while the owner keeps a paid Premium subscription.
- Only five test accounts can link Spotify until extended access is granted: enough for the demo, not for public users.