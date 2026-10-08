# 04: VPC and ALB Provisioning

**What to build:** Set up the network infrastructure and the Application Load Balancer (ALB) that will route public traffic to our containers. This creates the VPC (public subnets only to save costs) and the ALB with path-based routing rules.

**Blocked by:** 02-aws-foundation-oidc.md

**Type:** task
**Status:** ready-for-agent

- [ ] Terraform module `infra/modules/networking` is created (VPC, 2 public subnets, Internet Gateway, Security Groups).
- [ ] Terraform module `infra/modules/alb` is created (ALB, Target Groups for Backend and Frontend, HTTP Listener).
- [ ] ALB is configured with path-based routing: `/api/*` goes to the Backend Target Group, and `/*` goes to the Frontend Target Group.
- [ ] Networking and ALB modules are integrated into `infra/environments/staging/main.tf` and successfully applied.
