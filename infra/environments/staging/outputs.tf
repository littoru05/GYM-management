output "github_actions_role_arn" {
  value       = module.ci_oidc.role_arn
  description = "The ARN of the IAM role for GitHub Actions to assume via OIDC"
}

output "ecr_backend_repository_url" {
  value       = module.ecr.backend_repository_url
  description = "The URL of the backend ECR repository"
}

output "ecr_frontend_repository_url" {
  value       = module.ecr.frontend_repository_url
  description = "The URL of the frontend ECR repository"
}

