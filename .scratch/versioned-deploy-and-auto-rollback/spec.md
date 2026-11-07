# Artifact Versioning, Flyway Expand-Contract Migration, and Production Auto-Rollback

Status: ready-for-agent

## Problem Statement

Deploying new releases of the Gym Management system to production currently carries three operational risks across Frontend, Backend, and Database layers:

1. **Unversioned Artifacts and Stale Frontend Assets**: Container builds do not embed an explicit **Artifact Version**, making it impossible to verify via runtime endpoints which exact code revision is running. Additionally, while Nginx caches `/assets/` for one year, `index.html` lacks strict proxy/CDN cache-busting headers (`no-store`, `Pragma: no-cache`, `Expires: 0`), risking browsers serving a cached `index.html` that references outdated or mismatched JavaScript/CSS bundles after a release or rollback.
2. **Shallow Health Probes and Rollback Schema Lockout**: The backend currently relies on a static `/api/health` controller that returns `{"status": "UP"}` without verifying live MySQL connectivity or disk health, while Spring Boot Actuator is absent. Furthermore, if a newly deployed release applies a new Flyway migration and subsequently fails health checks, rolling back to the previous container image fails on startup because Flyway on the older image detects a "future" migration applied in `flyway_schema_history` that does not exist on the older image's classpath.
3. **Lack of Automated Pre-Migration Backups and Self-Healing Rollback Orchestration**: There is no automated orchestration script to capture and verify a non-locking logical SQL backup before migration, enforce a bounded backup retention window, poll deep health probes (`/actuator/health` and frontend HTTP status) within a strict verification timeout, and automatically revert containers to the **Last Stable Version** if the new release fails to become healthy.

## Solution

Introduce an end-to-end production release safety architecture combining **Artifact Versioning**, strict SPA cache control, **Expand-Contract Migration** with **Pre-Migration Backup**, deep Spring Boot Actuator health probes, and **Auto-Rollback** orchestration:

1. **Artifact Versioning & Nginx Cache Discipline**:
   - Standardize `APP_VERSION` (`<SemVer>-<GitShortSHA>`) across environment configurations, Dockerfiles, Docker Compose definitions, and the AWS production CD pipeline.
   - Inject `APP_VERSION` into the Backend runtime (`info.app.version` exposed via `/actuator/info`) and the Frontend build (`VITE_APP_VERSION` plus a static `/version.json` manifest).
   - Configure Nginx so `/index.html`, `/version.json`, and SPA fallback routes emit strict anti-caching headers (`Cache-Control: no-store, no-cache, must-revalidate, proxy-revalidate, max-age=0`, `Pragma: no-cache`, `Expires: 0`), while content-hashed files under `/assets/` are cached for 1 year (`public, max-age=31536000, immutable`).
2. **Flyway Expand-Contract & Automated Pre-Migration Backup**:
   - Configure Flyway across base and production profiles with `validate-on-migrate: true`, `out-of-order: true`, and `ignore-migration-patterns: ["*:future", "*:missing"]` so a rolled-back **Last Stable Version** container starts cleanly against an expanded database schema without rolling back live customer transactions.
   - Enforce backward-compatible schema rules via automated contract tests on `db/migration` scripts to prevent accidental destructive statements (`DROP TABLE`, `DROP COLUMN`, `RENAME COLUMN`, or un-defaulted `ADD COLUMN ... NOT NULL`) unless explicitly annotated for a contract phase.
   - Execute an automated, non-locking `mysqldump --single-transaction` **Pre-Migration Backup** compressed with `gzip -9` prior to container deployment, aborting immediately (fail-fast) if the backup is empty or invalid, and rotating snapshots to retain at most the 10 most recent backups.
3. **Deep Actuator Health Probes & Auto-Rollback Orchestration**:
   - Integrate `spring-boot-starter-actuator` exposing `/actuator/health` (with database and disk space indicators) and `/actuator/info` publicly through Spring Security and Nginx reverse proxy while preserving `/api/health` for backward compatibility.
   - Provide cross-platform deployment orchestrators (`scripts/deploy-prod.sh` and `scripts/deploy-prod.ps1`) paired with `docker-compose.prod.yml` that record the **Last Stable Version**, run the **Pre-Migration Backup**, start the target **Artifact Version**, poll `/actuator/health` and Frontend readiness every 5 seconds for up to 90 seconds (with immediate failure detection if the container crashes or exits), and trigger **Auto-Rollback** to the **Last Stable Version** on failure while supporting one-command manual database restoration (`--restore-backup`).

## User Stories

1. As a release engineer, I want every backend and frontend container build to accept an `APP_VERSION` build argument and embed it into the image labels and runtime metadata, so that every running container can be traced to its exact source revision.
2. As a site reliability engineer, I want `/actuator/info` on the backend and `/version.json` on the frontend to return the active **Artifact Version**, so that deployment scripts and operators can verify version parity between frontend and backend after deployment or rollback.
3. As an end user visiting the web application after a production deployment or **Auto-Rollback**, I want `index.html` and client-side SPA routes to never be cached by browsers or intermediate proxies, so that my browser immediately loads the HTML entrypoint matching the currently active release.
4. As an end user loading static bundles, I want content-hashed JavaScript, CSS, font, and image files under `/assets/` to be served with a 1-year `immutable` cache header, so that repeat page loads are fast and bandwidth-efficient without risking stale code execution.
5. As a DevOps engineer running health checks through either the backend port or the frontend Nginx reverse proxy, I want `/actuator/health` and `/actuator/info` proxied via dynamic DNS resolution in Nginx and permitted without JWT authentication in Spring Security, so that health probes succeed reliably in both Docker Compose and AWS ECS environments without breaking when backend hosts are unreachable at Nginx boot.
6. As a database administrator, I want Flyway configured with `validate-on-migrate: true` and `ignore-migration-patterns: ["*:future", "*:missing"]` across production profiles, so that existing migration checksums are verified while allowing an older application container to boot cleanly during **Auto-Rollback** when newer **Expand-Contract Migration** scripts have already been applied.
7. As a backend developer writing a new database migration, I want automated contract tests to flag destructive SQL operations (`DROP TABLE`, `DROP COLUMN`, `RENAME COLUMN`, or adding a `NOT NULL` column without a `DEFAULT` value) unless explicitly marked with a contract-phase annotation, so that no migration accidentally breaks backward compatibility with the **Last Stable Version**.
8. As a backend developer running existing unit tests, I want both `application-prod.yaml` and `application-prod.yml` to remain 100% compatible with existing HikariCP and production profile contract tests while sharing synchronized Flyway and Actuator settings, so that no legacy configuration contract regresses.
9. As a system administrator executing a production deployment, I want the deployment orchestrator to automatically capture a gzipped, non-locking MySQL logical dump (`--single-transaction --routines --triggers --events`) named with the current schema version, target **Artifact Version**, and timestamp before starting new containers, so that a verified recovery point always exists prior to any schema change.
10. As a system administrator, I want the deployment orchestrator to verify that the generated `.sql.gz` backup file is non-empty and contains a valid SQL dump header—aborting the deployment immediately before touching application containers if the backup fails—so that a release never proceeds without a valid **Pre-Migration Backup**.
11. As a system administrator managing disk space, I want the backup utility to automatically prune older backups and retain only the 10 most recent `.sql.gz` files in the backup directory, so that repeated deployments never exhaust server storage.
12. As a system administrator responding to a severe data corruption incident, I want a single-command flag (`--restore-backup <file.sql.gz>`) on the deployment script that restores the specified gzipped SQL dump into MySQL, so that emergency database recovery is fast, repeatable, and free of manual command errors.
13. As a release engineer deploying a new version via `deploy-prod.sh` or `deploy-prod.ps1`, I want the script to poll `/actuator/health` (verifying HTTP 200 and `"status":"UP"` including database readiness) and Frontend HTTP 200 every 5 seconds up to 90 seconds (18 attempts), while failing fast immediately if the backend container exits or enters a crash loop, so that broken releases are detected as quickly as possible.
14. As a release engineer, when a newly deployed **Artifact Version** passes all health checks, I want the script to update `.deploy-state` with `LAST_STABLE_VERSION=<APP_VERSION>` and append a `[SUCCESS]` entry to `.deploy-history.log`, so that the system records the new baseline for future rollbacks and auditing.
15. As a release engineer, when a newly deployed **Artifact Version** fails health checks or times out after 90 seconds, I want the script to automatically print the last 50 lines of backend logs, trigger **Auto-Rollback** by redeploying `LAST_STABLE_VERSION` via `docker-compose.prod.yml`, verify that the rolled-back services return to `"status":"UP"`, log a `[ROLLED_BACK]` event to `.deploy-history.log`, and exit with a non-zero status code, so that production self-heals without human intervention while alerting CI/CD of the failed release.
16. As a cloud engineer deploying to AWS ECS via GitHub Actions (`cd-prod.yml`), I want the Docker build steps for backend and frontend to pass `--build-arg APP_VERSION=$IMAGE_TAG` while preserving all existing workflow jobs, step names, and ECS deployment logic, so that ECR images carry version metadata without breaking existing pipeline contract tests.

## Implementation Decisions

- **Coexistence of AWS ECS Production and Self-Hosted Compose Production (ADR 0004)**: The repository supports both cloud production on AWS ECS Fargate + RDS (`cd-prod.yml`) and self-hosted / local production verification via `docker-compose.prod.yml` + `scripts/deploy-prod.{sh,ps1}`. Both share identical **Artifact Version** build arguments, Actuator health endpoints, Nginx caching rules, and Flyway **Expand-Contract Migration** settings.
- **Artifact Version Propagation**:
  - `.env.example` defines `DOCKER_REGISTRY`, `APP_VERSION`, `BACKUP_DIR`, `BACKUP_RETENTION_COUNT`, and `HEALTHCHECK_TIMEOUT_SECONDS`.
  - Backend `Dockerfile` declares `ARG APP_VERSION=1.0.0-dev`, sets `ENV APP_VERSION=${APP_VERSION}`, and applies OCI version labels. `application.yaml` binds `info.app.version: ${APP_VERSION:1.0.0-dev}`.
  - Frontend `Dockerfile` declares `ARG APP_VERSION=1.0.0-dev`, sets `ENV VITE_APP_VERSION=${APP_VERSION}` before `npm run build`, and writes `{"version":"${APP_VERSION}"}` to `/usr/share/nginx/html/version.json` in the Nginx runtime stage.
- **Nginx Cache Partitioning & Dynamic Upstream Proxy**:
  - Exact-match locations `location = /index.html` and `location = /version.json`, along with the SPA fallback `location /`, emit `Cache-Control: no-store, no-cache, must-revalidate, proxy-revalidate, max-age=0`, `Pragma: no-cache`, and `Expires: 0` with `always`.
  - `location ^~ /assets/` serves Vite's content-hashed bundles with `expires 1y` and `Cache-Control: public, max-age=31536000, immutable` alongside security headers (`X-Frame-Options`, `X-Content-Type-Options`, `X-XSS-Protection`).
  - `location ~ ^/actuator/(health|info)(/.*)?$` proxies to the backend using a runtime variable (`set $backend_upstream http://backend:8080; proxy_pass $backend_upstream;`) and Docker/AWS DNS resolver so Nginx never fails to boot when the `backend` hostname is absent in non-Compose topologies.
- **Spring Boot Actuator & Public Health Security**:
  - Add `spring-boot-starter-actuator` to `backend/pom.xml`.
  - Expose only `health,info` under `/actuator` (`management.endpoints.web.exposure.include: health,info`, `management.endpoint.health.show-details: always`, `management.endpoint.health.probes.enabled: true`, `management.info.env.enabled: true`).
  - Permit unauthenticated access to `/actuator/health`, `/actuator/health/**`, and `/actuator/info` in `SecurityConfig` while keeping `/api/health` intact.
- **Flyway Expand-Contract Configuration & Dual-File Prod Synchronization**:
  - Configure `spring.flyway` in `application.yaml`, `application-prod.yaml`, and `application-prod.yml` with `enabled: true`, `baseline-on-migrate: false`, `out-of-order: true`, `validate-on-migrate: true`, and `ignore-migration-patterns: ["*:future", "*:missing"]`.
  - Keep `classpath:db/migration` and `classpath:db/migration-dev` in the production profile locations list and preserve each file's respective HikariCP defaults (`10`/`2` in `application-prod.yaml` and `30`/`30` in `application-prod.yml`) so both `ProdProfileContractTest` and `HikariCpProdConfigTest` continue to pass.
  - Extend `FlywayMigrationOrderContractTest` to scan `classpath*:db/migration/V*.sql` and reject destructive schema statements (`DROP TABLE`, `DROP COLUMN`, `RENAME COLUMN`, or `ADD COLUMN ... NOT NULL` without `DEFAULT`) unless the script contains the explicit opt-in marker `-- @contract-phase: allow-breaking`.
- **Pre-Migration Backup, Retention & Auto-Rollback Scripts**:
  - Implement parity across `scripts/deploy-prod.sh` (Bash) and `scripts/deploy-prod.ps1` (PowerShell).
  - State files `.deploy-state`, `.deploy-history.log`, and directory `backups/` are added to `.gitignore`.
  - Before starting new application containers, the orchestrator verifies MySQL health, queries `MAX(version)` from `flyway_schema_history` (defaulting to `0` if uninitialized), streams `mysqldump --single-transaction --routines --triggers --events` through `gzip -9` into `${BACKUP_DIR}/gym_db_v${SCHEMA_VER}_to_${APP_VERSION}_${TIMESTAMP}.sql.gz`, verifies gzip integrity and non-zero size, and prunes backups beyond `BACKUP_RETENTION_COUNT` (default `10`).
  - During the verification window (default 18 polls x 5s = 90s), the script checks container crash states and polls `/actuator/health` for `"status":"UP"` plus Frontend HTTP 200. On failure, it automatically rolls back `backend` and `frontend` services to `LAST_STABLE_VERSION` recorded in `.deploy-state`, verifies recovery health, and exits with code `1`.

## Testing Decisions

- **Good Test Principle**: Tests must verify observable external contracts—HTTP status codes and JSON payloads from `/actuator/health` and `/actuator/info`, effective merged Spring properties, SQL migration backward-compatibility invariants, Nginx/Docker/CI configuration contracts, and deployment script state transitions—without coupling to private internal implementation details.
- **Tested Seams**:
  - **Seam 1 — Backend Configuration, Migration & Actuator Security Contract Seam (JUnit 5 / MockMvc)**:
    - `ActuatorHealthIntegrationTest`: Verifies `/actuator/health` and `/actuator/info` are accessible without JWT authentication (`200 OK`), report `"status": "UP"`, and expose `app.version`.
    - `ProdProfileContractTest` & `HikariCpProdConfigTest`: Verify Flyway `ignore-migration-patterns` (`*:future`, `*:missing`), `validate-on-migrate: true`, Actuator exposure, and HikariCP invariants across `application-prod.yaml` and `application-prod.yml`.
    - `FlywayMigrationOrderContractTest`: Verifies both idempotent seed inserts and **Expand-Contract Migration** safety rules across `db/migration/V*.sql`.
  - **Seam 2 — Container, Nginx & CI/CD Pipeline Contract Seam (Python `unittest` suite under `infra/tests/`)**:
    - Validates `APP_VERSION` build arguments in `backend/Dockerfile`, `frontend/Dockerfile`, `docker-compose.yml`, `docker-compose.prod.yml`, and `.github/workflows/cd-prod.yml` (while keeping all existing assertions in `test_prod_cd_pipeline.py` green).
    - Validates `frontend/nginx.conf` cache-control headers for `/index.html`, `/version.json`, and `/assets/`, plus dynamic `$backend_upstream` proxying for `/actuator/`.
  - **Seam 3 — Deployment, Backup & Auto-Rollback Orchestration Seam (Script Contract & Syntax Verification)**:
    - Validates syntax (`bash -n` and PowerShell AST parsing) and behavioral contracts of `scripts/deploy-prod.sh` and `scripts/deploy-prod.ps1`: pre-migration `mysqldump --single-transaction`, 10-file retention rotation, `/actuator/health` polling loop (5s interval, 90s timeout), automatic rollback to `LAST_STABLE_VERSION` on failure, and `--restore-backup` support.
- **Prior Art**:
  - Backend configuration contract tests in `backend/src/test/java/com/example/GYM_management_api/config/` (`ProdProfileContractTest.java`, `HikariCpProdConfigTest.java`, `FlywayMigrationOrderContractTest.java`).
  - Infrastructure and workflow contract tests in `infra/tests/` (`test_prod_cd_pipeline.py`, `test_ecs_and_cd_deployment.py`).

## Out of Scope

- Flyway Teams/Enterprise Undo migrations (`U<version>__*.sql`).
- Automatic destructive `mysqldump` restore on application healthcheck failure (kept strictly opt-in via `--restore-backup` to prevent wiping live customer transactions, per ADR 0004).
- Blue/Green dual-ALB or AWS CodeDeploy traffic shifting in AWS ECS (excluded to remain 100% within AWS Free Tier limits).

## Further Notes

- Uses the canonical domain vocabulary defined in `GLOSSARY.md` (`Artifact Version`, `Last Stable Version`, `Expand-Contract Migration`, `Pre-Migration Backup`, `Auto-Rollback`, `Pause`, `Hibernate`, `Resume`, `Dynamic DNS Binding`).
- Governed by `ADR 0001`, `ADR 0002`, `ADR 0003`, and `ADR 0004` (`docs/adr/0004-versioned-deploy-expand-contract-and-auto-rollback.md`).
