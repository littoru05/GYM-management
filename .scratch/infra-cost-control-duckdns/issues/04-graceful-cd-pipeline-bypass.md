# 04: Graceful CD Pipeline Bypass During Hibernation

**What to build:** Enable the continuous delivery pipeline to inspect container service existence before attempting deployment revisions. If the environment is hibernating, continue building and pushing images to the registry, but bypass service updates with a successful status and an informative notice.

**Blocked by:** 01-hibernate-targeted-teardown.md

**Status:** resolved

- [x] Inspect container service existence in the cluster before issuing update-service commands.
- [x] If services exist, proceed with standard rolling deployments.
- [x] If services are missing, skip update-service commands, report green build status, and output a notice to the job summary stating staging is hibernating and images will take effect upon resumption.
- [x] Verify continuous delivery workflow contracts with automated test assertions.

## Answer

- Updated `deploy-ecs` job in `.github/workflows/cd-staging.yml` for both `backend` and `frontend` deployment steps.
- Wrapped `aws ecs update-service` in an existence check: queries `aws ecs describe-services --cluster $ECS_CLUSTER --services $SERVICE --query 'services[0].status'` for `ACTIVE` state.
- When services are not found or inactive (hibernated), the workflow skips `update-service`, logs a GitHub notice (`::notice::`), and outputs markdown notification to `$GITHUB_STEP_SUMMARY` confirming images were built and pushed to ECR and will automatically activate on next `resume`.
- Added `test_cd_staging_workflow_graceful_hibernate_bypass` in `infra/tests/test_ecs_and_cd_deployment.py`. All 27 unit tests pass.
