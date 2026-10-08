# 06: Pause and Resume Workflow

**What to build:** Provide a simple, single-click mechanism for the team to pause the staging environment to save costs on Fargate compute when it is not actively being tested.

**Blocked by:** 05-ecs-fargate-deployment.md

**Type:** task
**Status:** ready-for-agent

- [ ] A new manual GitHub Actions workflow `.github/workflows/infra-control.yml` is created with a `workflow_dispatch` trigger.
- [ ] The workflow accepts an input action: `pause` or `resume`.
- [ ] When `pause` is selected, the workflow scales the desired count of both ECS services down to 0.
- [ ] When `resume` is selected, the workflow scales the desired count of both ECS services back to 1.
