# 01: Backend Actuator Health Probes, Version Info & Flyway Expand-Contract Safety

**What to build:** Expose deep production readiness and version metadata endpoints on the backend service via Spring Boot Actuator (`/actuator/health` and `/actuator/info`) without requiring JWT authentication, while preserving `/api/health` for backward compatibility. Bind the application version to the runtime **Artifact Version** (`APP_VERSION`), and configure Flyway across base and production profiles to enforce **Expand-Contract Migration** compatibility (`validate-on-migrate: true` and `ignore-migration-patterns: ["*:future", "*:missing"]`) so that a rolled-back **Last Stable Version** container can boot cleanly against an expanded database schema without failing validation. Extend the automated backend test suite to verify public Actuator endpoints, synchronized dual-file production profile contracts, and backward-compatible SQL migration rules.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] Add Spring Boot Actuator support to the backend and configure `/actuator/health` (with database and disk health details and probes enabled) and `/actuator/info` (exposing the application name and `APP_VERSION`).
- [ ] Permit unauthenticated public access to `/actuator/health`, `/actuator/health/**`, and `/actuator/info` in the security filter chain while keeping `/api/health` active.
- [ ] Configure Flyway in the base and both production profile files with `validate-on-migrate: true`, `out-of-order: true`, and `ignore-migration-patterns` set to ignore `*:future` and `*:missing` migrations, while preserving each production file's existing HikariCP connection pool defaults.
- [ ] Extend the SQL migration contract test suite to reject destructive statements (`DROP TABLE`, `DROP COLUMN`, `RENAME COLUMN`, or adding a `NOT NULL` column without a `DEFAULT`) in core schema migrations unless explicitly marked with `-- @contract-phase: allow-breaking`.
- [ ] Add an integration test verifying that `/actuator/health` and `/actuator/info` respond with HTTP 200 without authentication and return `"status": "UP"` along with the configured version metadata, and ensure all existing backend contract tests pass.
