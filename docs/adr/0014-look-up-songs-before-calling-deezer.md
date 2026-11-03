# ADR-0014: Look Up Songs in the Database Before Calling Deezer

**Date:** 2026-10-08
**Status:** Accepted

## Context

In the Stage 3 design, dropping a track always calls Deezer (`GET /track/{id}`) to get its ISRC, then looks for the song in the database by ISRC. All Deezer calls share one quota of about 50 requests every 5 seconds for the server (ADR-0011), and many drops will reuse songs that are already stored.

## Decision

The drops service first looks for the song in the database by `deezer_track_id`, and only calls Deezer when it is not found. Deezer then gives the ISRC, used to find the same recording stored under another Deezer id, before creating a new song.

## Consequences

**Positive:**
- Dropping a song that is already stored makes no call to Deezer, which saves quota and time.
- The ISRC still matches the same recording across albums, singles and compilations.

**Trade-offs:**
- A stored song's title and cover are not refreshed from Deezer when it is dropped again.
- The order differs from sequence diagram 5.2 of the Stage 3 document.