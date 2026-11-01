# 04: Production Continuous Delivery Pipeline on Main Branch with Hibernate Bypass

**What to build:** Deliver an automated continuous delivery workflow triggered on pushes to the `main` branch (and manual dispatch) that validates the production infrastructure code, builds and pushes backend and frontend container images tagged with `prod-<sha>` and `prod-latest` to the shared ECR repositories in parallel, updates the production ECS task definitions, and deploys to active production ECS services—or gracefully skips service updates with an informative summary notice when the production environment is in **Hibernate**.

**Blocked by:** 01-prod-spring-profile-and-db-tuning.md, 03-prod-environment-composition-and-guardrails.md

**Status:** resolved

- [x] Trigger the production continuous delivery workflow on pushes to the `main` branch and on `workflow_dispatch`.
- [x] Run formatting checks, initialization, and validation against the production infrastructure configuration.
- [x] Build and push backend and frontend Docker images in parallel to the shared ECR repositories using `prod-<sha>` and `prod-latest` tags, and update the staging workflow to also tag `staging-<sha>` so ECR lifecycle rules cleanly separate both environments.
- [x] Register new revisions of the `gym-prod-backend` and `gym-prod-frontend` task definitions with the newly pushed `prod-<sha>` image URIs.
- [x] Inspect whether `gym-prod-backend` and `gym-prod-frontend` ECS services are `ACTIVE` before updating them; if absent or inactive due to **Hibernate**, bypass service updates without failing the workflow and write a clear status notice to the GitHub Step Summary.
- [x] Add automated workflow contract tests verifying triggers, parallel image builds, `prod-` tagging, task definition registration, and graceful **Hibernate** bypass.

## Answer

- **Production Continuous Delivery Workflow (`.github/workflows/cd-prod.yml`)**:
  - Triggers on push to `main` branch and manual execution via `workflow_dispatch`.
  - Configures AWS credentials and setup for Terraform (`hashicorp/setup-terraform@v3`).
  - Executes `terraform fmt -check`, `terraform init`, and `terraform validate` against `infra/environments/prod`.
  - Builds and pushes backend and frontend Docker images in parallel to shared ECR repositories (`gym-backend` and `gym-frontend`) tagged with `prod-$IMAGE_TAG` (`prod-<sha>`) and `prod-latest`.
  - Dynamically updates container images in ECS task definitions (`gym-prod-backend` and `gym-prod-frontend`) and registers new task definition revisions.
  - Queries ECS service status via `aws ecs describe-services` targeting `gym-prod-cluster`:
    - When `ACTIVE`: Triggers blue/green rolling deployment via `aws ecs update-service --force-new-deployment`.
    - When absent or inactive (**Hibernate**): Gracefully skips service updates with zero exit failure and publishes a Vietnamese status notice to `$GITHUB_STEP_SUMMARY` explaining that the image and task revision are ready for subsequent resume.
- **Shared ECR Lifecycle Separation (`.github/workflows/cd-staging.yml`)**:
  - Updated staging backend and frontend image build jobs to tag and push `staging-$IMAGE_TAG` alongside `<sha>` and `latest`, ensuring complete isolation under ECR lifecycle policy prefix rules.
- **Automated Workflow Verification (`infra/tests/test_prod_cd_pipeline.py`)**:
  - Authored 8 contract tests asserting triggers, permissions, prod directory validation, parallel build jobs, prod tagging, task definition updates, graceful Hibernate bypass, and staging tag separation.
  - All 86 infrastructure unit and contract tests passing (`Ran 86 tests in 0.025s - OK`).
