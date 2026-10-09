# 04: VPC and ALB Provisioning

**What to build:** Set up the network infrastructure and the Application Load Balancer (ALB) that will route public traffic to our containers. This creates the VPC (public subnets only to save costs) and the ALB with path-based routing rules.

**Blocked by:** 02-aws-foundation-oidc.md

**Type:** task
**Status:** resolved

- [x] Terraform module `infra/modules/networking` is created (VPC, 2 public subnets, Internet Gateway, Security Groups).
- [x] Terraform module `infra/modules/alb` is created (ALB, Target Groups for Backend and Frontend, HTTP Listener).
- [x] ALB is configured with path-based routing: `/api/*` goes to the Backend Target Group, and `/*` goes to the Frontend Target Group.
- [x] Networking and ALB modules are integrated into `infra/environments/staging/main.tf` and successfully applied.

## Answer

- Created Terraform module `infra/modules/networking` provisioning VPC (`10.0.0.0/16`), 2 public subnets across availability zones for high availability and ALB compliance, Internet Gateway, public Route Table, and security groups (`alb-sg` for port 80 and `ecs-sg` for ports 8080/80 restricted from ALB).
- Created Terraform module `infra/modules/alb` provisioning an Application Load Balancer, backend Target Group (`port: 8080, target_type: ip, health_check: /api/health`), frontend Target Group (`port: 80, target_type: ip, health_check: /`), HTTP port 80 Listener with default forwarding to frontend, and a priority-based routing rule directing `/api` and `/api/*` to the backend Target Group.
- Integrated `networking` and `alb` modules into `infra/environments/staging/main.tf` and exposed `vpc_id`, `public_subnet_ids`, `alb_security_group_id`, `ecs_security_group_id`, `alb_dns_name`, and Target Group ARNs in `infra/environments/staging/outputs.tf`.

