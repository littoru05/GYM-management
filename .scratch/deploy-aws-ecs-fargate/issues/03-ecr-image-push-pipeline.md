# 03: ECR Provisioning and Image Push Pipeline

**What to build:** Automate the building and storing of container images. The infrastructure must provide Amazon ECR repositories, and the GitHub Actions workflow must build the Docker images and push them to ECR whenever code is merged into `develop`.

**Blocked by:** 01-dockerization-health-check.md, 02-aws-foundation-oidc.md

**Type:** task
**Status:** resolved

- [x] Terraform module `infra/modules/ecr` is created to provision two repositories (`gym-backend`, `gym-frontend`) with a lifecycle policy keeping the 5 most recent images.
- [x] The `ecr` module is integrated into the staging environment (`infra/environments/staging/main.tf`).
- [x] `cd-staging.yml` is updated to include jobs for building both the frontend and backend Docker images in parallel.
- [x] `cd-staging.yml` pushes the built images to the newly provisioned ECR repositories using AWS credentials (AWS Access Keys per commit f261c13).

## Answer

- Created Terraform module `infra/modules/ecr` provisioning `gym-backend` and `gym-frontend` ECR repositories with lifecycle policies keeping the 5 most recent images and image scan on push.
- Integrated `ecr` module in `infra/environments/staging/main.tf` and exported repository URLs in `infra/environments/staging/outputs.tf`.
- Updated `.github/workflows/cd-staging.yml` with parallel jobs `build-and-push-backend` and `build-and-push-frontend` running concurrently after `terraform-init`, authenticating via AWS credentials, logging into ECR, and building and pushing both SHA and `latest` tags.
