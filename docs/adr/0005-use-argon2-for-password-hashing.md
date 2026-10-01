# ADR-0005: Use Argon2 for Password Hashing

**Date:** 2026-10-01
**Status:** Accepted

## Context

The technical documentation planned bcrypt for password hashing. bcrypt only uses the first 72 bytes of a password and, since version 5, raises an error above that limit, so the code has to truncate passwords the same way at registration and at login. The project has no real users yet, so changing the algorithm only means recreating the test database.

## Decision

Passwords are hashed with Argon2id through the `argon2-cffi` library, using its default settings. Hashing and verification are grouped in `app/utils/security.py`.

## Consequences

**Positive:**
- Argon2id is the first choice recommended by OWASP for new applications, and it resists GPU attacks better because it uses a lot of memory.
- No 72-byte limit, so there is no truncation to keep identical between registration and login.
- Simpler API: it works directly with strings and generates the salt automatically.

**Trade-offs:**
- Each hash uses 64 MiB of memory with the default settings; `memory_cost` may need to be lowered on a small production server.
- Hashes created with bcrypt can no longer be verified, so the test database had to be recreated.