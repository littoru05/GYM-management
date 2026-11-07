# 02: Container Artifact Versioning, Nginx Cache Partitioning & Production Compose Stack

**What to build:** Standardize **Artifact Version** (`APP_VERSION`) propagation across container builds, environment templates, Docker Compose configurations, and the AWS production continuous delivery workflow. Configure the frontend container build to embed `VITE_APP_VERSION` and generate a static `/version.json` manifest, and harden Nginx caching rules so `/index.html`, `/version.json`, and SPA fallback routes are never cached by browsers or proxies while hashed bundles under `/assets/` are cached immutably for one year. Add a dynamic-resolver Nginx reverse proxy rule for `/actuator/health` and `/actuator/info`, provide a dedicated `docker-compose.prod.yml` stack wired to versioned images and `/actuator/health` container healthchecks, and verify all container, Nginx, and CI/CD contracts with automated tests.

**Blocked by:** 01: Backend Actuator Health Probes, Version Info & Flyway Expand-Contract Safety

**Status:** ready-for-agent

- [ ] Document `DOCKER_REGISTRY`, `APP_VERSION`, `BACKUP_DIR`, `BACKUP_RETENTION_COUNT`, and `HEALTHCHECK_TIMEOUT_SECONDS` in the environment example file, and ignore `.deploy-state`, `.deploy-history.log`, and `backups/` in Git.
- [ ] Update the backend and frontend Dockerfiles to accept `ARG APP_VERSION`, expose runtime version metadata, and emit `/usr/share/nginx/html/version.json` during the frontend image build.
- [ ] Configure Nginx with strict no-cache headers (`no-store, no-cache, must-revalidate, proxy-revalidate, max-age=0`, `Pragma: no-cache`, `Expires: 0`) on `= /index.html`, `= /version.json`, and `/` SPA fallbacks, 1-year `immutable` caching on `^~ /assets/`, and dynamic `$backend_upstream` reverse proxying for `/actuator/health` and `/actuator/info`.
- [ ] Create `docker-compose.prod.yml` (and align `docker-compose.yml`) to tag and run images using `${DOCKER_REGISTRY}/gym-backend:${APP_VERSION}` and `${DOCKER_REGISTRY}/gym-frontend:${APP_VERSION}` with the `prod` profile and `/actuator/health` container health checks.
- [ ] Pass `--build-arg APP_VERSION=$IMAGE_TAG` in the production GitHub Actions workflow (`cd-prod.yml`) without altering existing job/step names or ECS deployment behavior.
- [ ] Add automated infrastructure contract tests verifying Dockerfile version arguments, Nginx cache and proxy directives, `docker-compose.prod.yml` invariants, and `cd-prod.yml` compatibility.
