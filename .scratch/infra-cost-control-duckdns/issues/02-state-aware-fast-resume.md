# 02: State-Aware Fast Resume with Load Balancer Detection

**What to build:** Enable the `resume` action to automatically inspect whether the load balancer exists using cloud provider CLI tools before running configuration management. If the load balancer is present (resuming from `pause`), immediately scale container tasks up in under 30 seconds without modifying infrastructure; if absent (resuming from `hibernate`), run infrastructure provisioning to rebuild the load balancer and container services.

**Blocked by:** 01-hibernate-targeted-teardown.md

**Status:** resolved

- [x] Inspect Application Load Balancer existence using cloud CLI tools before running configuration management.
- [x] If ALB exists, scale backend and frontend container services desired count to 1 directly.
- [x] If ALB is missing, initialize and apply infrastructure configurations to recreate the load balancer and container services.
- [x] Update contract test suite to verify conditional execution paths for pause vs hibernate resumption.

## Answer

- Added `Check Infrastructure State (ALB Existence)` step via `aws elbv2 describe-load-balancers --names gym-staging-alb`.
- Implemented fast-path branch `Resume from Pause`: scales backend and frontend services to desired-count 1 directly within ~15 seconds without running Terraform when ALB exists.
- Implemented full-path branch `Resume from Hibernate`: runs `terraform init` and `terraform apply -auto-approve` to rebuild the ALB, target groups, and ECS services when ALB does not exist.
- Added `test_workflow_resume_state_aware_logic` to `infra/tests/test_infra_control.py` validating CLI inspection and Terraform apply branching. All 25 unit tests pass.
