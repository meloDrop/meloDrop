# ADR-0004: Split Interactions into Saves and Skips Tables

**Date:** 2026-09-30
**Status:** Accepted

## Context

The Stage 3 documentation planned one `interactions` table with an `action`
column (`save` or `skip`). A save needs to store the platform used
(`provider`), a skip does not. In a single table, `provider` would be nullable
and only useful for half of the rows.

## Decision

Replace `interactions` with two tables: `saves` (with `provider NOT NULL`) and
`skips`. Both have a unique constraint on `(user_id, drop_id)` and foreign keys
with `ON DELETE CASCADE`.

## Consequences

**Positive:**
- `provider` is required for every save, enforced by the database.
- Stats and library read `saves` directly, without filtering.

**Trade-offs:**
- The database no longer prevents saving and skipping the same drop. The
  service must check this.
- The Discover feed must exclude drops from both tables.