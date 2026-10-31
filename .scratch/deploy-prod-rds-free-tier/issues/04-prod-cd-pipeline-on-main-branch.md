# 04: Production Continuous Delivery Pipeline on Main Branch with Hibernate Bypass

**What to build:** Deliver an automated continuous delivery workflow triggered on pushes to the `main` branch (and manual dispatch) that validates the production infrastructure code, builds and pushes backend and frontend container images tagged with `prod-<sha>` and `prod-latest` to the shared ECR repositories in parallel, updates the production ECS task definitions, and deploys to active production ECS services—or gracefully skips service updates with an informative summary notice when the production environment is in **Hibernate**.

**Blocked by:** 01-prod-spring-profile-and-db-tuning.md, 03-prod-environment-composition-and-guardrails.md

**Status:** ready-for-agent

- [ ] Trigger the production continuous delivery workflow on pushes to the `main` branch and on `workflow_dispatch`.
- [ ] Run formatting checks, initialization, and validation against the production infrastructure configuration.
- [ ] Build and push backend and frontend Docker images in parallel to the shared ECR repositories using `prod-<sha>` and `prod-latest` tags, and update the staging workflow to also tag `staging-<sha>` so ECR lifecycle rules cleanly separate both environments.
- [ ] Register new revisions of the `gym-prod-backend` and `gym-prod-frontend` task definitions with the newly pushed `prod-<sha>` image URIs.
- [ ] Inspect whether `gym-prod-backend` and `gym-prod-frontend` ECS services are `ACTIVE` before updating them; if absent or inactive due to **Hibernate**, bypass service updates without failing the workflow and write a clear status notice to the GitHub Step Summary.
- [ ] Add automated workflow contract tests verifying triggers, parallel image builds, `prod-` tagging, task definition registration, and graceful **Hibernate** bypass.
