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

output "vpc_id" {
  value       = module.networking.vpc_id
  description = "The ID of the VPC"
}

output "public_subnet_ids" {
  value       = module.networking.public_subnet_ids
  description = "The IDs of the public subnets"
}

output "alb_security_group_id" {
  value       = module.networking.alb_security_group_id
  description = "The security group ID of the ALB"
}

output "ecs_security_group_id" {
  value       = module.networking.ecs_security_group_id
  description = "The security group ID of the ECS tasks"
}

output "alb_arn" {
  value       = module.alb.alb_arn
  description = "The ARN of the Application Load Balancer"
}

output "alb_dns_name" {
  value       = module.alb.alb_dns_name
  description = "The DNS name of the Application Load Balancer"
}

output "backend_target_group_arn" {
  value       = module.alb.backend_target_group_arn
  description = "The ARN of the backend target group"
}

output "frontend_target_group_arn" {
  value       = module.alb.frontend_target_group_arn
  description = "The ARN of the frontend target group"
}
