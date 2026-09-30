# ADR-0003: Named Docker Volume for MySQL Data

**Date:** 2026-09-28
**Status:** Accepted

## Context

Using a bind mount (`./db:/var/lib/mysql`) causes Docker to create a local
`db/` folder in the repository containing MySQL binary files. This pollutes
the project tree and can cause permission issues on Windows/WSL2.

## Decision

MySQL data is stored in a named Docker volume (`mysql_data`). No local
folder is created for database files.

## Consequences

**Positive:**
- No binary data in the repository.
- Docker handles the volume lifecycle cleanly.
- `docker compose down -v` resets everything when needed.

**Trade-offs:**
- The data is not directly accessible from the host filesystem — inspection
  requires `docker compose exec db`.