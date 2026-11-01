# 03: Production Environment Composition with Shared Singletons and Free-Tier Guardrail Tests

**What to build:** Compose the complete production infrastructure environment (`prod`) combining an isolated VPC (`10.1.0.0/16`), Application Load Balancer, Single-AZ RDS MySQL instance, and downsized ECS Fargate Spot services (`256 CPU / 512 MiB` for both backend and frontend without a MySQL sidecar) while sharing account-level singletons (remote state S3 bucket and DynamoDB lock table under a dedicated `prod` state key, shared ECR repositories via data lookups, shared OIDC provider, and the existing account-wide cost budget without creating a third budget).

**Blocked by:** 02-isolated-db-networking-rds-and-ecs-prefactor.md

**Status:** resolved

- [x] Configure the production remote backend to store state under a dedicated production key in the existing S3 state bucket and DynamoDB lock table.
- [x] Provision isolated production networking (`10.1.0.0/16`) with two public subnets for the load balancer and Fargate tasks and two isolated private database subnets for RDS.
- [x] Wire the production Application Load Balancer, Single-AZ RDS MySQL module (`gym-prod-mysql`), and ECS Fargate module (`gym-prod-cluster`) with `enable_mysql_sidecar = false`, `spring_profile = "prod"`, and backend task sizing reduced to `256 CPU / 512 MiB`.
- [x] Reference the existing `gym-backend` and `gym-frontend` ECR repositories via data sources and configure the production CI OIDC role using the existing account OIDC provider.
- [x] Omit creating a third AWS Budget resource in the production environment so the AWS account stays strictly within the two-budget Free Tier limit.
- [x] Expose production outputs (ALB DNS name, RDS endpoint, ECS cluster and service names, and CloudWatch log groups) and verify the environment with `terraform fmt -check`, `terraform validate`, and automated contract tests.

## Answer

- **Production Remote Backend (`infra/environments/prod/backend.tf`)**:
  - Configured S3 remote backend using shared account bucket `gym-management-tfstate-staging` and DynamoDB lock table `gym-management-tflock-staging` under dedicated key `prod/terraform.tfstate`.
- **Shared Singleton Resources & Guardrails (`infra/environments/prod/main.tf`)**:
  - Referenced existing ECR repositories (`gym-backend` and `gym-frontend`) via `aws_ecr_repository` data lookups rather than creating duplicate registry modules.
  - Configured `module "ci_oidc"` with `create_oidc_provider = false` to reuse the account-wide GitHub Actions OIDC provider.
  - Omitted `module "cost_monitoring"` / `aws_budgets_budget` to strictly respect the two-budget AWS Free Tier limit.
- **Isolated Production Networking**:
  - Configured `module "networking"` with `vpc_cidr = "10.1.0.0/16"`, public subnets `["10.1.1.0/24", "10.1.2.0/24"]`, and isolated database subnets `["10.1.10.0/24", "10.1.11.0/24"]` (`enable_database_subnets = true`).
- **Production Compute & Database Wiring**:
  - Wired `module "alb"` for `gym-prod-alb` to VPC and public subnets.
  - Provisioned `module "rds"` (`gym-prod-mysql`) in isolated database subnets attached to the database security group.
  - Configured `module "ecs"` (`gym-prod-cluster`) with `enable_mysql_sidecar = false`, `spring_profile = "prod"`, downsized tasks (`256 CPU / 512 MiB` for backend and frontend), and connected directly to the RDS instance address and credentials.
- **Production Outputs (`infra/environments/prod/outputs.tf`)**:
  - Exposed ALB DNS name, RDS endpoint and address, ECS cluster and service names, task definition ARNs, and consolidated CloudWatch log groups.
- **Automated Verification**:
  - Added 9 automated contract tests in `infra/tests/test_prod_environment_composition.py`.
  - Verified with `terraform fmt -check` (clean formatting) and `terraform validate` (`Success! The configuration is valid.`).
  - Ran full test suites: 78/78 Python infrastructure contract tests passed, and 361/361 Maven tests passed.
