# ADR-0006: Group Schemas by Module

**Date:** 2026-10-01
**Status:** Accepted

## Context

The technical documentation splits the backend into modules (auth, drops, feed...), each with its own routes, service and schemas file. The router and service of the auth module were named `auth.py`, but its first schemas were placed in `schemas/user.py`, named after the user entity. Adding the login schemas raised the question of where they belong.

## Decision

Pydantic schemas are grouped by module, like routers and services. All auth module schemas live in `schemas/auth.py`, and `schemas/user.py` is removed. SQLAlchemy models keep one file per table (`models/user.py`).

## Consequences

**Positive:**
- Each module has the same file name in `routers/`, `services/` and `schemas/`, so the code of a feature is easy to find.
- The code follows the structure defined in the technical documentation.

**Trade-offs:**
- A schema needed by several modules (for example a user representation reused by profile) has to be imported from the module that owns it.
- Models and schemas follow different naming rules: `models/user.py` sits next to `schemas/auth.py`.