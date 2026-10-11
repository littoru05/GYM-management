output "role_arn" {
  value       = aws_iam_role.github_actions.arn
  description = "The ARN of the IAM Role created for GitHub Actions OIDC authentication"
}

output "role_name" {
  value       = aws_iam_role.github_actions.name
  description = "The name of the IAM Role for GitHub Actions"
}

output "oidc_provider_arn" {
  value       = aws_iam_openid_connect_provider.github.arn
  description = "The ARN of the GitHub OIDC provider"
}
