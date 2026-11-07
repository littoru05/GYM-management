# Flyway Expand-Contract and Pre-Migration SQL Backup for Auto-Rollback

For production deployments (both local/self-hosted Compose production verification and AWS ECS Fargate releases), we handle release failures by combining **Expand-Contract Migration** (`ignore-migration-patterns: *:future, *:missing`) with an automated **Pre-Migration Backup** (`mysqldump --single-transaction` with a 10-snapshot retention ring) and container **Auto-Rollback** to the **Last Stable Version**, rather than executing automated database down-migrations or restoring the SQL dump automatically on healthcheck failure.

We chose this architecture because:
- **Zero transaction loss on rollback**: Automatically restoring a pre-deployment SQL dump or running destructive `DROP` undo scripts when a health check fails after 60 seconds would wipe out any member check-ins, subscriptions, or POS orders written during the startup window.
- **Flyway Community compatibility**: Flyway Undo migrations (`U__`) require Flyway Teams/Enterprise licensing and fail whenever a migration involves irreversible data transformations.
- **Forward-and-backward schema compatibility**: Enforcing additive-only `EXPAND` migrations (nullable columns or columns with defaults, no `DROP`/`RENAME` without an explicit contract marker) and configuring `ignore-migration-patterns: ["*:future", "*:missing"]` allows the previous stable container image (`LAST_STABLE_VERSION`) to start cleanly against a database that has already applied the newer release's schema version.
- **Fail-safe disaster recovery**: Capturing a verified `.sql.gz` logical dump before every deployment (and aborting deployment immediately if the backup fails) guarantees a point-in-time recovery artifact via an explicit operator command (`--restore-backup`) if a migration ever corrupts data despite contract tests.

## Considered Options

- **Automated Database Restore on Healthcheck Failure**: Restoring the pre-migration `.sql.gz` dump automatically whenever `/actuator/health` fails. Rejected because it destroys live transactions committed between migration completion and healthcheck timeout, and causes downtime during restore.
- **Flyway Undo Migrations (`U<version>__*.sql`)**: Writing paired rollback SQL scripts for every forward migration. Rejected because it is unsupported in Flyway Community Edition and introduces high risk of data loss when dropping newly added columns or tables.
- **Strict Flyway Validation without Ignoring Future Migrations**: Keeping Flyway's default `ignore-migration-patterns` behavior. Rejected because when Auto-Rollback reverts the backend container to `LAST_STABLE_VERSION`, the older image lacks the newly applied migration file on its classpath and crashes on startup with a Flyway validation error (`Detected applied migration not resolved locally`).

## Consequences

- Every new schema migration in `db/migration` must adhere to the Expand phase (additive and backward-compatible with the immediately preceding release) unless explicitly marked for a verified Contract phase (`-- @contract-phase: allow-breaking`).
- When Auto-Rollback triggers, the application reverts to `LAST_STABLE_VERSION` while the database schema remains at the expanded version; if operators determine that data itself was corrupted, they can invoke the one-command `--restore-backup <file.sql.gz>` procedure using the Pre-Migration Backup.
