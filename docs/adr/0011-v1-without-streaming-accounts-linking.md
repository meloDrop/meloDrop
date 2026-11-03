# ADR-0011: V1 Without Streaming Account Linking

**Date:** 2026-10-06
**Status:** Accepted

## Context

Since ADR-0010, saving a track requires a linked Spotify account, which only works for five test accounts and needs the owner's paid Premium subscription. Extended access is out of reach, because Spotify now grants it only to organizations with at least 250,000 monthly active users. Spotify also no longer gives 30-second previews to new apps, while the public Deezer API still offers search, track details with the ISRC and previews without a developer account.

## Decision

In V1, meloDrop does not link streaming accounts: search, track details and previews come from the public Deezer API, and liked tracks are saved in playlists that users create inside meloDrop. Spotify linking and exporting playlists to Spotify move to V2, so the links router is unplugged, its code stays in the repository, and ADR-0010 is superseded.

## Consequences

**Positive:**
- Anyone can sign up and use the whole app, with no test account limit and no paid subscription.
- V1 has no OAuth tokens to store, encrypt or refresh.
- Each song keeps its ISRC, so V2 can find liked tracks on Spotify exactly.

**Trade-offs:**
- Until V2, liked tracks only live in meloDrop: users cannot play them in full in their streaming app.
- All Deezer calls share one quota of about 50 requests every 5 seconds for the server's IP.
- Preview links expire after about 15 minutes, so the backend must fetch a new one each time a track is played.