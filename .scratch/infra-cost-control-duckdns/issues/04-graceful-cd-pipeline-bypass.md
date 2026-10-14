# 04: Graceful CD Pipeline Bypass During Hibernation

**What to build:** Enable the continuous delivery pipeline to inspect container service existence before attempting deployment revisions. If the environment is hibernating, continue building and pushing images to the registry, but bypass service updates with a successful status and an informative notice.

**Blocked by:** 01-hibernate-targeted-teardown.md

**Status:** ready-for-agent

- [ ] Inspect container service existence in the cluster before issuing update-service commands.
- [ ] If services exist, proceed with standard rolling deployments.
- [ ] If services are missing, skip update-service commands, report green build status, and output a notice to the job summary stating staging is hibernating and images will take effect upon resumption.
- [ ] Verify continuous delivery workflow contracts with automated test assertions.
