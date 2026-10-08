# 02: Bootstrap Terraform State and GitHub OIDC Auth

**What to build:** Establish the foundation for Infrastructure as Code (IaC) and CI/CD security. This involves creating the S3/DynamoDB backend for Terraform state, writing a Terraform module for OIDC to allow GitHub Actions to authenticate without static credentials, and proving it works with a basic workflow.

**Blocked by:** None (can start immediately)

**Type:** task
**Status:** ready-for-agent

- [ ] S3 bucket and DynamoDB table for Terraform state are created (can be done manually via AWS CLI or bootstrap script).
- [ ] Terraform configuration `infra/environments/staging/backend.tf` is configured to use the S3 backend.
- [ ] Terraform module `infra/modules/ci-oidc` is created to provision the AWS OIDC Identity Provider and an IAM Role for GitHub Actions.
- [ ] A starter GitHub Actions workflow (`.github/workflows/cd-staging.yml`) is created that successfully assumes the IAM role via OIDC and runs `terraform init`.
