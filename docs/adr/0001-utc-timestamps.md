# ADR-0001: Store All Timestamps in UTC

**Date:** 2026-09-28
**Status:** Accepted

## Context

The app runs in Docker and could be deployed on servers in different time
zones. MySQL can be set to store timestamps in local time, but this creates
inconsistencies between environments.

## Decision

All timestamps are stored in UTC. The `--default-time-zone` flag is not set
in the MySQL Docker service. Time zone conversion is handled at the API or
client level when needed.

## Consequences

**Positive:**
- Consistent timestamps across all environments.
- Standard practice for production applications.

**Trade-offs:**
- Time zone conversion must be done explicitly in the API or mobile client
  when displaying dates to the user.