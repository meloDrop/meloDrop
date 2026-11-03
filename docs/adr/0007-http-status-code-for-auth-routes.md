# ADR-0007: HTTP Status Codes for Auth Routes

**Date:** 2026-10-02
**Status:** Accepted

## Context

The technical documentation lists the status codes used by the API, but not when to use each one. 
While building the login route, a wrong password returned 400 and was documented as 409. 
The Android app will log the user out on any 401, so the meaning of each code must be clear.

## Decision

`POST /auth/register` returns 201 when the account is created and 409 when the email or username is already taken. 
`POST /auth/login` returns 200 with the token, and 401 with the same message whether the identifier is unknown or the password is wrong; both routes return 422 when the data does not match the schema.

## Consequences

**Positive:**
- Each code has one meaning: 401 means "not authenticated", 409 means "conflicts with existing data".
- The login does not reveal which accounts exist.

**Trade-offs:**
- Codes for the next routes (403, 404, 204...) are not covered yet and will need their own decision.