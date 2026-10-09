# 06: Pause and Resume Workflow

**What to build:** Provide a simple, single-click mechanism for the team to pause the staging environment to save costs on Fargate compute when it is not actively being tested.

**Blocked by:** 05-ecs-fargate-deployment.md

**Type:** task
**Status:** resolved

- [x] A new manual GitHub Actions workflow `.github/workflows/infra-control.yml` is created with a `workflow_dispatch` trigger.
- [x] The workflow accepts an input action: `pause` or `resume`.
- [x] When `pause` is selected, the workflow scales the desired count of both ECS services down to 0.
- [x] When `resume` is selected, the workflow scales the desired count of both ECS services back to 1.

## Answer

- Created manual GitHub Actions workflow `.github/workflows/infra-control.yml` triggered via `workflow_dispatch` with a choice input `action` (`pause` or `resume`).
- Configured AWS authentication using repository secrets (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`).
- When `pause` is triggered, the workflow updates both ECS services (`gym-staging-backend` and `gym-staging-frontend`) setting `--desired-count 0`, immediately freeing compute resources and eliminating Fargate running costs.
- When `resume` is triggered, the workflow updates both ECS services setting `--desired-count 1`, recreating the containers and restoring the staging environment with demo data seeded on startup via Flyway.

