# 02: Isolated Database Networking, Single-AZ Free-Tier RDS Module, and Conditional ECS Sidecar Prefactoring

**What to build:** Extend the shared infrastructure modules and create a dedicated relational database module so an environment can opt into an external, 100% Free-Tier Single-AZ RDS MySQL instance inside isolated private subnets while keeping the existing ephemeral MySQL sidecar behavior as the default for staging. This includes isolated database subnets without NAT gateways, strict security group rules permitting port 3306 exclusively from application containers, a reusable Single-AZ RDS MySQL 8.4 module locked to free-tier limits, a conditional sidecar toggle in the container orchestration module, shared OIDC provider lookup with RDS lifecycle permissions, and environment-prefixed container registry lifecycle rules so staging builds never evict production images.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

- [ ] Extend the networking module to optionally provision two isolated private database subnets across distinct availability zones with an internal-only route table (no NAT gateway), a database subnet group, and a database security group allowing inbound TCP port 3306 exclusively from the ECS task security group.
- [ ] Create a reusable relational database module that provisions a Single-AZ MySQL 8.4 micro instance (`db.t4g.micro`) with 20 GB `gp2` storage, disabled storage autoscaling (`max_allocated_storage = 0`), disabled `multi_az`, disabled `publicly_accessible`, 7-day backup retention, disabled Performance Insights, and exported database endpoint/address outputs.
- [ ] Parameterize the ECS module with an `enable_mysql_sidecar` toggle (defaulting to `true`), `db_host`, `db_username`, and `spring_profile` inputs; when `enable_mysql_sidecar` is `false`, omit the MySQL sidecar container and health-check dependency from the backend task definition and wire the backend container directly to the external database host.
- [ ] Update the CI OIDC module to support referencing an existing account-wide OIDC provider via data lookup when creation is disabled, and grant IAM permissions to describe, start, and stop RDS database instances.
- [ ] Update the container registry lifecycle rules so environment-tagged production images (`prod-*`) are retained independently of staging pushes while keeping total stored images within the 500 MB free-tier quota.
- [ ] Add automated infrastructure contract tests for the RDS module, isolated database networking, and conditional ECS sidecar toggle, while ensuring 100% of existing staging unit tests continue to pass without modification.
