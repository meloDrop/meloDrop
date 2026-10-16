# ADR-0016: Brevo for Sending Emails

**Date:** 2026-10-15
**Status:** Accepted

## Context

meloDrop sends verification and password reset codes by email (ADR-0015). Emails sent from an unknown server often end up in spam, and the setup should not have to change between development and production.

## Decision

The backend sends all emails through the Brevo SMTP relay, in development and in production, with Python's `smtplib`. The Brevo login, key and sender address are read from the `.env` file.

## Consequences

**Positive:**
- Real delivery from the start, improved by SPF and DKIM once the meloDrop domain is set up.
- Same code and same service in every environment: only the `.env` values change.
- The free plan (300 emails per day) covers development and the first users.

**Trade-offs:**
- Tests send real emails: they must use addresses we own, and they count against the daily quota.
- The sign-up flow needs an internet connection and valid Brevo credentials, even locally.