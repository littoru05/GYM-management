# 05: ECS Services and CD Pipeline Integration

**What to build:** Complete the continuous delivery loop. Provision the ECS Cluster and Fargate services (including the MySQL ephemeral sidecar) and update the CI/CD pipeline to deploy the new container images to ECS automatically.

**Blocked by:** 03-ecr-image-push-pipeline.md, 04-core-network-alb.md

**Type:** task
**Status:** resolved

- [x] Terraform module `infra/modules/ecs` is created (ECS Cluster, Fargate Spot Task Definitions, ECS Services).
- [x] Backend Task Definition includes both the Spring Boot container and the MySQL 8.4 sidecar container.
- [x] Backend container securely references `DB_PASSWORD` and `JWT_SECRET` from AWS SSM Parameter Store.
- [x] Frontend Task Definition is created for the React/Nginx container.
- [x] `cd-staging.yml` is updated to register the new Task Definitions with the newly pushed image tags and update the ECS services.
- [x] Accessing the ALB DNS URL successfully loads the frontend and `/api/health` returns 200 OK (configured via ALB path rules and target group health checks).

## Answer

- Created Terraform module `infra/modules/ecs` provisioning:
  - ECS Cluster with `FARGATE_SPOT` default capacity provider strategy for cost efficiency.
  - Backend Task Definition running Spring Boot 4 along with a `mysql:8.4` ephemeral sidecar container with health check (`mysqladmin ping`), container dependency (`condition: HEALTHY`), `SPRING_PROFILES_ACTIVE=dev` for Flyway auto-migration/seeding, and secure secrets injection for `DB_PASSWORD` and `JWT_SECRET` from AWS SSM Parameter Store (`/gym/staging/*`).
  - Frontend Task Definition running the React/Nginx container exposing port 80.
  - CloudWatch Log Groups for backend, mysql, and frontend with 7-day retention.
  - IAM Task Execution and Task roles with policies for ECR image pull, CloudWatch logs, and SSM parameter reads.
  - ECS Services for backend (port 8080) and frontend (port 80) operating on Fargate Spot in public subnets with `assign_public_ip = true` and attached to their respective ALB target groups.
- Integrated `ecs` module into `infra/environments/staging/main.tf` and exported cluster name, ID, service names, and task definition ARNs in `infra/environments/staging/outputs.tf`.
- Updated `.github/workflows/cd-staging.yml` with `deploy-ecs` job running after image builds that downloads existing task definitions, updates container image tags with Git commit SHA, registers new revisions, and updates both ECS services with `--force-new-deployment`.

