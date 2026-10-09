# 01: Containerize Frontend, Backend and add Health Check

**What to build:** Ensure the application can run in a standalone containerized environment. This includes a multi-stage Docker build for the Spring Boot backend, a multi-stage Docker build for the React frontend running on Nginx (with SPA routing), and a simple health check endpoint in the backend for the ALB to use later.

**Blocked by:** None (can start immediately)

**Type:** task
**Status:** resolved

- [x] Backend `Dockerfile` is created using a multi-stage build (Maven -> JRE) and exposes port 8080.
- [x] A simple `GET /api/health` endpoint is implemented in the backend, returning `{"status": "UP"}`.
- [x] Frontend `Dockerfile` is created using a multi-stage build (Node -> Nginx).
- [x] Frontend `nginx.conf` is configured to correctly route SPA paths using `try_files` and exposes port 80.
- [x] Both containers build successfully locally.
