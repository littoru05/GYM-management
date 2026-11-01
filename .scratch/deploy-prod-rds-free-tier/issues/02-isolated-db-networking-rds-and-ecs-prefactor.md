# 02: Isolated Database Networking, Single-AZ Free-Tier RDS Module, and Conditional ECS Sidecar Prefactoring

Type: task
Status: resolved
Blocked by: none

**What to build:** Extend the shared infrastructure modules and create a dedicated relational database module so an environment can opt into an external, 100% Free-Tier Single-AZ RDS MySQL instance inside isolated private subnets while keeping the existing ephemeral MySQL sidecar behavior as the default for staging. This includes isolated database subnets without NAT gateways, strict security group rules permitting port 3306 exclusively from application containers, a reusable Single-AZ RDS MySQL 8.4 module locked to free-tier limits, a conditional sidecar toggle in the container orchestration module, shared OIDC provider lookup with RDS lifecycle permissions, and environment-prefixed container registry lifecycle rules so staging builds never evict production images.

- [x] Extend the networking module to optionally provision two isolated private database subnets across distinct availability zones with an internal-only route table (no NAT gateway), a database subnet group, and a database security group allowing inbound TCP port 3306 exclusively from the ECS task security group.
- [x] Create a reusable relational database module that provisions a Single-AZ MySQL 8.4 micro instance (`db.t4g.micro`) with 20 GB `gp2` storage, disabled storage autoscaling (`max_allocated_storage = 0`), disabled `multi_az`, disabled `publicly_accessible`, 7-day backup retention, disabled Performance Insights, and exported database endpoint/address outputs.
- [x] Parameterize the ECS module with an `enable_mysql_sidecar` toggle (defaulting to `true`), `db_host`, `db_username`, and `spring_profile` inputs; when `enable_mysql_sidecar` is `false`, omit the MySQL sidecar container and health-check dependency from the backend task definition and wire the backend container directly to the external database host.
- [x] Update the CI OIDC module to support referencing an existing account-wide OIDC provider via data lookup when creation is disabled, and grant IAM permissions to describe, start, and stop RDS database instances.
- [x] Update the container registry lifecycle rules so environment-tagged production images (`prod-*`) are retained independently of staging pushes while keeping total stored images within the 500 MB free-tier quota.
- [x] Add automated infrastructure contract tests for the RDS module, isolated database networking, and conditional ECS sidecar toggle, while ensuring 100% of existing staging unit tests continue to pass without modification.

## Answer

- **Isolated Database Networking (`infra/modules/networking`)**:
  - Added `enable_database_subnets` (default `false`) and `database_subnet_cidrs` (default `["10.0.10.0/24", "10.0.11.0/24"]`).
  - Implemented `aws_subnet.database` with `map_public_ip_on_launch = false`, an internal-only `aws_route_table.database` with no NAT Gateway or Internet Gateway routes, and `aws_db_subnet_group.database`.
  - Added `aws_security_group.database` restricting inbound port 3306 exclusively to `aws_security_group.ecs.id`.
  - Exported `database_subnet_ids`, `database_subnet_group_name`, `database_subnet_group_id`, and `database_security_group_id`.
- **Reusable Single-AZ Free-Tier RDS Module (`infra/modules/rds`)**:
  - Implemented `aws_db_instance.main` configured with MySQL 8.4 LTS, `db.t4g.micro`, 20 GB `gp2` storage, disabled autoscaling (`max_allocated_storage = 0`), disabled `multi_az`, disabled `publicly_accessible`, 7-day backup retention, disabled Performance Insights, and `skip_final_snapshot = true`.
  - Exported `db_instance_id`, `db_instance_identifier`, `db_instance_address`, `db_instance_endpoint`, `db_instance_port`, `db_instance_arn`, `db_name`, and `db_username`.
- **Conditional ECS Sidecar Prefactoring (`infra/modules/ecs`)**:
  - Added variables `enable_mysql_sidecar` (default `true`), `db_host` (default `"127.0.0.1"`), `db_port` (default `3306`), `db_name` (default `"gym_management"`), `db_username` (default `"root"`), and `spring_profile` (default `"dev"`).
  - Conditionally excluded the MySQL sidecar container definition and `dependsOn` dependency when `enable_mysql_sidecar = false`, directly wiring Spring Boot to the external database host.
  - Made CloudWatch MySQL log group and outputs conditional on `enable_mysql_sidecar`.
- **CI OIDC Module Existing Provider Lookup & RDS Permissions (`infra/modules/ci-oidc`)**:
  - Added `create_oidc_provider` (default `true`) and `oidc_provider_arn` (default `""`) variables with `data.aws_iam_openid_connect_provider.github` lookup when creation is disabled.
  - Granted `rds:DescribeDBInstances`, `rds:StartDBInstance`, and `rds:StopDBInstance` actions in the CI/CD execution policy.
- **ECR Repository Lifecycle Rules (`infra/modules/ecr`)**:
  - Added priority 1 rule targeting `prod-*` tagged images (`max_prod_image_count = 3`) using `tagPrefixList` and `imageCountMoreThan`.
  - Configured priority 2 rule targeting other/staging images (`max_image_count = 3`), keeping total images safely under the 500 MB Free Tier quota while insulating production images from staging build churn.
- **Automated Infrastructure Contract Tests**:
  - Created `infra/tests/test_rds_and_prefactor.py` with 20 contract tests covering all 5 seams.
  - Verified 100% pass rate across all 69 infrastructure tests and existing Java contract tests with complete backwards compatibility for staging.
