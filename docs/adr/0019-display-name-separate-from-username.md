# ADR-0019: Display Name Separate From the Username

**Date:** 2026-10-22
**Status:** Accepted

## Context

The username is unique, it is used to log in, and it was the only name shown in the app. Users want to choose how their name appears on their profile without changing how they log in. Two users must still be told apart on the Discover cards.

## Decision

Each account gets an optional `display_name` (30 characters at most, not unique), changed with `PATCH /me/profile`. The profile shows it above the @username, the Discover cards show the @username, and an empty display name shows the username instead.

## Consequences

**Positive:**
- Users can change their public name freely, and the login stays the same.
- The unique @username keeps every user easy to identify on the cards.

**Trade-offs:**
- Two names to show and to explain, and two users can have the same display name.
- The display name is public free text with no report button, like the username.