# 05: ECS Services and CD Pipeline Integration

**What to build:** Complete the continuous delivery loop. Provision the ECS Cluster and Fargate services (including the MySQL ephemeral sidecar) and update the CI/CD pipeline to deploy the new container images to ECS automatically.

**Blocked by:** 03-ecr-image-push-pipeline.md, 04-core-network-alb.md

**Type:** task
**Status:** ready-for-agent

- [ ] Terraform module `infra/modules/ecs` is created (ECS Cluster, Fargate Spot Task Definitions, ECS Services).
- [ ] Backend Task Definition includes both the Spring Boot container and the MySQL 8.4 sidecar container.
- [ ] Backend container securely references `DB_PASSWORD` and `JWT_SECRET` from AWS SSM Parameter Store.
- [ ] Frontend Task Definition is created for the React/Nginx container.
- [ ] `cd-staging.yml` is updated to register the new Task Definitions with the newly pushed image tags and update the ECS services.
- [ ] Accessing the ALB DNS URL successfully loads the frontend and `/api/health` returns 200 OK.
