# 02: State-Aware Fast Resume with Load Balancer Detection

**What to build:** Enable the `resume` action to automatically inspect whether the load balancer exists using cloud provider CLI tools before running configuration management. If the load balancer is present (resuming from `pause`), immediately scale container tasks up in under 30 seconds without modifying infrastructure; if absent (resuming from `hibernate`), run infrastructure provisioning to rebuild the load balancer and container services.

**Blocked by:** 01-hibernate-targeted-teardown.md

**Status:** ready-for-agent

- [ ] Inspect Application Load Balancer existence using cloud CLI tools before running configuration management.
- [ ] If ALB exists, scale backend and frontend container services desired count to 1 directly.
- [ ] If ALB is missing, initialize and apply infrastructure configurations to recreate the load balancer and container services.
- [ ] Update contract test suite to verify conditional execution paths for pause vs hibernate resumption.
