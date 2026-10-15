# 01: Hibernate Option with Targeted Destruction and Contract Tests

**What to build:** Provide the ability for developers to trigger a `hibernate` action in the infrastructure control workflow that selectively tears down hourly-billed resources (Application Load Balancer, target groups, and container services) via targeted destruction while preserving network foundations and container images, bringing idle running costs to $0.00.

**Blocked by:** None (can start immediately)

**Status:** resolved

- [x] Add `hibernate` choice to `workflow_dispatch` action input in the control workflow.
- [x] Add a `hibernate` step executing targeted destruction of ALB, target groups, and ECS services with auto-approval.
- [x] Record a detailed breakdown in the execution step summary detailing destroyed resources and $0.00 hourly spend.
- [x] Update and expand the contract test suite to assert the presence and behavior of the `hibernate` action and its targeted flags.

## Answer

- Added `hibernate` as an input choice to `workflow_dispatch` in `.github/workflows/infra-control.yml`.
- Added the `Hibernate` step utilizing `terraform destroy` targeted at `module.ecs.aws_ecs_service.backend`, `module.ecs.aws_ecs_service.frontend`, and `module.alb` with `-auto-approve`.
- Configured rich GitHub Step Summary output confirming destroyed components, retained scaffolding (ECR, VPC, ECS Cluster, Task Definitions, SSM Parameters), and $0.00/hour idle running cost.
- Expanded `infra/tests/test_infra_control.py` with `test_workflow_hibernate_logic` testing target options and summary reporting. All tests pass (24/24).
