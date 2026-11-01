# Deploy Production RDS Free Tier

This effort provisions a production environment on AWS with a Single-AZ RDS MySQL instance (100% Free Tier compliant), CI/CD pipeline on the `main` branch, and environment-aware lifecycle management (Hibernate with stop-in-place RDS, Pause, and Resume with DuckDNS binding).

## Tickets

- [01-prod-spring-profile-and-db-tuning.md](./issues/01-prod-spring-profile-and-db-tuning.md)
- [02-isolated-db-networking-rds-and-ecs-prefactor.md](./issues/02-isolated-db-networking-rds-and-ecs-prefactor.md)
- [03-prod-environment-composition-and-guardrails.md](./issues/03-prod-environment-composition-and-guardrails.md)
- [04-prod-cd-pipeline-on-main-branch.md](./issues/04-prod-cd-pipeline-on-main-branch.md)
- [05-multi-env-lifecycle-control-rds-and-duckdns.md](./issues/05-multi-env-lifecycle-control-rds-and-duckdns.md)

## Decisions so far
- Decided on Single-AZ RDS MySQL (`db.t4g.micro` or `db.t3.micro`) with 20 GB `gp2` storage and stop-on-hibernate for the production environment ([ADR 0003](../../docs/adr/0003-rds-mysql-single-az-free-tier.md)).
- Reusing account singleton resources (OIDC provider, ECR repos, account budget) to avoid exceeding AWS Free Tier limits and duplicate resource conflicts ([spec.md](./spec.md)).
- Configured dedicated production Spring Boot profile (`application-prod.yaml`) tuned for Single-AZ RDS MySQL (`sslMode=PREFERRED`, HikariCP 10 max / 2 min idle, CloudWatch SQL log suppression, Flyway schema + demo seed migrations) with contract tests ([01-prod-spring-profile-and-db-tuning.md](./issues/01-prod-spring-profile-and-db-tuning.md)).
- Prefactored core infrastructure modules (networking with isolated private database subnets and internal route table, Single-AZ RDS MySQL 8.4 module with Free Tier constraints, conditional ECS sidecar and external DB host wiring, existing OIDC provider lookup with RDS lifecycle permissions, and ECR lifecycle policies protecting `prod-*` tagged images) with comprehensive contract tests ([02-isolated-db-networking-rds-and-ecs-prefactor.md](./issues/02-isolated-db-networking-rds-and-ecs-prefactor.md)).
- Composed the production environment (`infra/environments/prod`) integrating isolated networking (`10.1.0.0/16`), ALB, Single-AZ RDS MySQL (`gym-prod-mysql`), and downsized ECS Fargate Spot services (`256 CPU / 512 MiB`), while sharing account-level singletons (remote state, ECR data lookups, account OIDC provider) and omitting duplicate budget resources to stay within the 2-budget Free Tier ceiling ([03-prod-environment-composition-and-guardrails.md](./issues/03-prod-environment-composition-and-guardrails.md)).
- Implemented production continuous delivery workflow (`.github/workflows/cd-prod.yml`) triggered on `main` and `workflow_dispatch`, performing production Terraform validation, parallel Docker container builds with `prod-<sha>` and `prod-latest` tags, task definition revisions, and graceful Hibernate bypass when ECS services are inactive, with staging tagged with `staging-<sha>` for clean ECR lifecycle separation ([04-prod-cd-pipeline-on-main-branch.md](./issues/04-prod-cd-pipeline-on-main-branch.md)).
