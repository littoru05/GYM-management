# 03: ECR Provisioning and Image Push Pipeline

**What to build:** Automate the building and storing of container images. The infrastructure must provide Amazon ECR repositories, and the GitHub Actions workflow must build the Docker images and push them to ECR whenever code is merged into `develop`.

**Blocked by:** 01-dockerization-health-check.md, 02-aws-foundation-oidc.md

**Type:** task
**Status:** ready-for-agent

- [ ] Terraform module `infra/modules/ecr` is created to provision two repositories (`gym-backend`, `gym-frontend`) with a lifecycle policy keeping the 5 most recent images.
- [ ] The `ecr` module is integrated into the staging environment (`infra/environments/staging/main.tf`).
- [ ] `cd-staging.yml` is updated to include jobs for building both the frontend and backend Docker images in parallel.
- [ ] `cd-staging.yml` pushes the built images to the newly provisioned ECR repositories using the OIDC role.
