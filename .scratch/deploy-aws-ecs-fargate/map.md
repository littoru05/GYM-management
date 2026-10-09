# Deploy AWS ECS Fargate

This effort deploys the GYM Management app to AWS ECS Fargate using Terraform and GitHub Actions.

## Tickets

- [01-dockerization-health-check.md](./issues/01-dockerization-health-check.md)
- [02-aws-foundation-oidc.md](./issues/02-aws-foundation-oidc.md)
- [03-ecr-image-push-pipeline.md](./issues/03-ecr-image-push-pipeline.md)
- [04-core-network-alb.md](./issues/04-core-network-alb.md)
- [05-ecs-fargate-deployment.md](./issues/05-ecs-fargate-deployment.md)
- [06-environment-cost-control.md](./issues/06-environment-cost-control.md)

## Decisions so far
- Spec created at [spec.md](./spec.md)
- ECR repositories and parallel image push pipeline provisioned ([03-ecr-image-push-pipeline.md](./issues/03-ecr-image-push-pipeline.md))
- Core networking VPC and Application Load Balancer with path-based routing provisioned ([04-core-network-alb.md](./issues/04-core-network-alb.md))
- ECS Fargate cluster, task definitions with MySQL ephemeral sidecar, and automated CD deployment pipeline provisioned ([05-ecs-fargate-deployment.md](./issues/05-ecs-fargate-deployment.md))
- Environment pause/resume workflow created for cost control ([06-environment-cost-control.md](./issues/06-environment-cost-control.md))




