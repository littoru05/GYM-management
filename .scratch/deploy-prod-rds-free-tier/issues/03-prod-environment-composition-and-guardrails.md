# 03: Production Environment Composition with Shared Singletons and Free-Tier Guardrail Tests

**What to build:** Compose the complete production infrastructure environment (`prod`) combining an isolated VPC (`10.1.0.0/16`), Application Load Balancer, Single-AZ RDS MySQL instance, and downsized ECS Fargate Spot services (`256 CPU / 512 MiB` for both backend and frontend without a MySQL sidecar) while sharing account-level singletons (remote state S3 bucket and DynamoDB lock table under a dedicated `prod` state key, shared ECR repositories via data lookups, shared OIDC provider, and the existing account-wide cost budget without creating a third budget).

**Blocked by:** 02-isolated-db-networking-rds-and-ecs-prefactor.md

**Status:** ready-for-agent

- [ ] Configure the production remote backend to store state under a dedicated production key in the existing S3 state bucket and DynamoDB lock table.
- [ ] Provision isolated production networking (`10.1.0.0/16`) with two public subnets for the load balancer and Fargate tasks and two isolated private database subnets for RDS.
- [ ] Wire the production Application Load Balancer, Single-AZ RDS MySQL module (`gym-prod-mysql`), and ECS Fargate module (`gym-prod-cluster`) with `enable_mysql_sidecar = false`, `spring_profile = "prod"`, and backend task sizing reduced to `256 CPU / 512 MiB`.
- [ ] Reference the existing `gym-backend` and `gym-frontend` ECR repositories via data sources and configure the production CI OIDC role using the existing account OIDC provider.
- [ ] Omit creating a third AWS Budget resource in the production environment so the AWS account stays strictly within the two-budget Free Tier limit.
- [ ] Expose production outputs (ALB DNS name, RDS endpoint, ECS cluster and service names, and CloudWatch log groups) and verify the environment with `terraform fmt -check`, `terraform validate`, and automated contract tests.
