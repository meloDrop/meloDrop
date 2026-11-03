# ADR-0002: Pydantic Settings Without Field Aliases

**Date:** 2026-09-28
**Status:** Accepted

## Context

Pydantic Settings allows mapping environment variable names to Python
attribute names using `Field(alias=...)`. An earlier version of `config.py`
used this to bridge the `MYSQL_*` naming required by the MySQL Docker image
and the `DB_*` naming used in Python.

## Decision

No aliases in `Settings`. The `.env` file uses `DB_*` variable names.
MySQL-specific variables (`MYSQL_ROOT_PASSWORD`, `MYSQL_DATABASE`) are set
in the `environment` block of `docker-compose.yml` using `${}` interpolation
from the same `.env` file.

## Consequences

**Positive:**
- `config.py` is simple and easy to read.
- The mapping between `.env` and Docker is visible in `docker-compose.yml`.

**Trade-offs:**
- Two variable names exist for the same value (`DB_PASSWORD` and
  `MYSQL_ROOT_PASSWORD`). This is intentional.