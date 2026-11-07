# 03: Pre-Migration SQL Backup & Self-Healing Auto-Rollback Orchestration Scripts

**What to build:** Deliver cross-platform production deployment orchestrators (`scripts/deploy-prod.sh` for Linux/CI and `scripts/deploy-prod.ps1` for Windows) that automate the complete safe-release lifecycle: reading the **Last Stable Version** from `.deploy-state`, capturing and validating a compressed, non-locking **Pre-Migration Backup** (`mysqldump --single-transaction` to `.sql.gz`) with a 10-snapshot retention ring, launching the new **Artifact Version** via `docker-compose.prod.yml`, polling `/actuator/health` and frontend HTTP readiness every 5 seconds for up to 90 seconds (with immediate crash-loop detection), recording successful releases to `.deploy-state` and `.deploy-history.log`, or automatically executing **Auto-Rollback** to `LAST_STABLE_VERSION` upon failure while providing a one-command `--restore-backup` option for emergency database recovery.

Blocked by: 02-container-versioning-nginx-cache-and-prod-compose

Status: resolved

- [x] Implement `scripts/deploy-prod.sh` and `scripts/deploy-prod.ps1` supporting target `APP_VERSION` resolution (from CLI argument, environment variable, or `<SemVer>-<GitShortSHA>`) and reading `LAST_STABLE_VERSION` from `.deploy-state`.
- [x] Before starting updated application containers, verify MySQL readiness, query the current schema version from `flyway_schema_history`, capture a non-locking compressed SQL dump (`mysqldump --single-transaction --routines --triggers --events | gzip -9`), abort immediately (fail-fast) if the backup fails or is empty, and prune older backups beyond the 10 most recent `.sql.gz` files.
- [x] Support a `--restore-backup <file.sql.gz>` flag in both scripts that validates the archive and restores the SQL dump into the running MySQL container with a single command.
- [x] Deploy the target `APP_VERSION` with `docker-compose.prod.yml` and poll `/actuator/health` (`"status":"UP"`) and frontend HTTP 200 every 5 seconds up to 90 seconds (18 attempts), failing fast if the backend container exits or restarts unexpectedly.
- [x] On healthcheck success, persist `LAST_STABLE_VERSION=${APP_VERSION}` in `.deploy-state` and append a `[SUCCESS]` entry to `.deploy-history.log`.
- [x] On healthcheck timeout or failure, print the last 50 lines of backend logs, trigger **Auto-Rollback** by redeploying `LAST_STABLE_VERSION`, verify that the rolled-back stack returns to `"status":"UP"`, record a `[ROLLED_BACK]` entry in `.deploy-history.log`, and exit with status code `1`.
- [x] Add automated contract and syntax tests verifying both `deploy-prod.sh` and `deploy-prod.ps1` across backup, retention, health polling, auto-rollback, and restore-backup invariants.
