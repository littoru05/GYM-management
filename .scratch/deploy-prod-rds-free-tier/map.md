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
