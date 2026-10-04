# ADR-0008: Fetch Preview Links on Demand

**Date:** 2026-10-04
**Status:** Accepted

## Context

The technical documentation planned to store Deezer preview links in a `preview_urls` table for 12 hours. A test of the Deezer API showed that these links contain an expiration date set about 15 minutes after the request, so a stored link would stop working long before being reused.

## Decision

Preview links are no longer stored: the feed service asks Deezer for a fresh link for each drop it returns. The `preview_urls` table, its model and the `PREVIEW_URL_TTL_HOURS` setting are removed.

## Consequences

**Positive:**
- The app always receives a link that works.
- Less code and one table less to maintain.

**Trade-offs:**
- One call to Deezer for each drop in the feed, which makes the response slower.
- The Deezer public API limits the number of requests per IP address, which could become a problem with many users.