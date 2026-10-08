Status: ready-for-agent

## Problem Statement

The GYM Management application (consisting of a Spring Boot backend and a React SPA frontend) currently lacks a deployment environment and a CI/CD pipeline. Developers need a staging environment on AWS that automatically deploys changes pushed to the `develop` branch, enabling them to test the integrated application in a cloud environment that mirrors production architecture, while keeping infrastructure costs as low as possible.

## Solution

Containerize both the frontend and backend applications using Docker and deploy them to AWS ECS Fargate (using Spot instances for cost savings) managed by Terraform. A single Application Load Balancer (ALB) will route traffic to both services using path-based routing. The database will run as an ephemeral MySQL sidecar container within the backend task to eliminate the cost of a managed RDS instance during staging. GitHub Actions will handle the CI/CD pipeline, securely connecting to AWS via OIDC to build and push images to ECR and update the ECS services.

## User Stories

1. As a developer, I want my code pushed to the `develop` branch to be automatically deployed to a staging environment, so that I can immediately verify changes.
2. As a QA tester, I want to access the frontend via a stable ALB URL, so that I can test the GYM management web app without running it locally.
3. As a DevOps engineer, I want the infrastructure to be provisioned using Terraform, so that it is reproducible and version-controlled.
4. As a DevOps engineer, I want to use AWS ECS Fargate Spot instances for the staging environment, so that infrastructure costs are minimized.
5. As a developer, I want the backend to automatically have demo data seeded upon deployment, so that I don't have to manually populate the database for testing.
6. As an administrator, I want a single-click workflow to pause and resume the infrastructure, so that I can save costs when the environment is not actively being tested.
7. As a security engineer, I want the application secrets stored securely in AWS SSM Parameter Store, so that credentials are not hardcoded or leaked in the repository.
8. As a DevOps engineer, I want OIDC authentication between GitHub Actions and AWS, so that I don't have to manage long-lived AWS IAM credentials.

## Implementation Decisions

- **Containerization**: The frontend (React/Vite) and backend (Spring Boot 4) will be built as two separate Docker images.
- **Backend Docker**: Will use a multi-stage build (Maven build -> JRE runtime) exposing port 8080.
- **Frontend Docker**: Will use a multi-stage build (Node build -> Nginx runtime) and configure Nginx for SPA routing (`try_files`) exposing port 80.
- **Database Strategy**: MySQL 8.4 will run as an ephemeral sidecar container in the same ECS Task as the backend. This eliminates RDS costs and networking complexity for staging.
- **Data Seeding**: The `dev` Spring profile will be used to automatically apply Flyway migrations and insert demo data on startup to populate the ephemeral database.
- **Terraform Structure**: The codebase will use a module-based structure (`networking`, `ecr`, `ecs`, `alb`, `ci-oidc`) located in `infra/modules` and `infra/environments/staging`.
- **Terraform State**: Will be stored remotely in an S3 bucket with a DynamoDB table for state locking.
- **Networking**: The VPC will consist of public subnets only, without NAT Gateways, to reduce costs.
- **ALB Routing**: A single Application Load Balancer will use path-based routing: `/api/*` targets the Backend, and `/*` targets the Frontend.
- **Frontend Configuration**: The frontend application will use relative URLs (`VITE_API_BASE_URL=/api`) to communicate with the API, avoiding CORS configuration issues.
- **CI/CD Workflows**: Workflows will be split: `ci.yml` for pull request compile checks, and a new `cd-staging.yml` for pushing to `develop`.
- **Health Check**: A custom `/api/health` endpoint will be added to the backend specifically for ALB health checks.
- **Cost Control**: A manual GitHub Actions workflow (`infra-control.yml`) will be created to scale ECS desired counts down to 0 to pause the environment.

## Testing Decisions

Good tests for this infrastructure should verify that the external behaviors (routing, container startup, deployment triggers) work as expected without relying on internal implementation details.

- **Docker Build Seam**: The `backend/Dockerfile` and `frontend/Dockerfile` will be tested locally. The backend must compile and start, and the frontend Nginx routing must function correctly for SPA paths.
- **Infrastructure Plan Seam**: The `terraform plan` output will be reviewed to ensure the expected resources (VPC, ECS Cluster, ALB, OIDC) are generated correctly prior to execution.
- **Deployment Verification Seam**: The `/api/health` endpoint accessed through the ALB DNS must return a 200 OK, verifying that the ALB, ECS Task, Spring Boot app, and ephemeral MySQL database are all functioning and connected.
- **CI/CD Pipeline Seam**: GitHub Actions workflows will be verified by observing correct triggers (CI on PR, CD on push to `develop`) and ensuring parallel build steps execute successfully before the ECS deployment step.

## Out of Scope

- Deploying to a production environment (this will be handled in a future phase using the `main` branch, RDS, and NAT Gateways).
- Configuring a custom domain and HTTPS/SSL certificates for the staging environment (the default AWS ALB DNS name will be used).
- Persistent database storage for the staging environment.

## Further Notes

- The detailed deployment plan and cost estimations have been generated and discussed during the planning phase.
- An ADR (`docs/adr/0001-mysql-ephemeral-sidecar.md`) was created to document the deliberate choice to use an ephemeral MySQL sidecar instead of a managed database for the staging environment.
