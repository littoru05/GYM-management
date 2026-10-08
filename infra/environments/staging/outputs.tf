output "github_actions_role_arn" {
  value       = module.ci_oidc.role_arn
  description = "The ARN of the IAM role for GitHub Actions to assume via OIDC"
}
