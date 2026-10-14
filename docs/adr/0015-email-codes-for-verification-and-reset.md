# ADR-0015: Email Codes for Account Verification and Password Reset

**Date:** 2026-10-14
**Status:** Accepted

## Context

meloDrop must verify a user's email address at registration and let users reset a forgotten password. A link in an email must open an address the phone can reach, but the backend only runs locally for now. Opening the Android app directly from a link would also require a domain, HTTPS and App Links setup.

## Decision

The backend emails a short-lived 6-digit code, and the user types it in the app to verify their email or set a new password. Mailpit catches the emails during development, and a real SMTP service will send them in production.

## Consequences

**Positive:**
- Works with a local backend and the emulator, without a public address or a domain.
- The whole flow stays in the app, with no web page to build.

**Trade-offs:**
- The user has to open their mailbox and copy the code by hand.
- A 6-digit code can be guessed, so codes must expire and the number of attempts must be limited.