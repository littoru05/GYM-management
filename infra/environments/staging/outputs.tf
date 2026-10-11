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

output "ecs_cluster_name" {
  value       = module.ecs.cluster_name
  description = "The name of the ECS cluster"
}

output "ecs_cluster_id" {
  value       = module.ecs.cluster_id
  description = "The ID of the ECS cluster"
}

output "backend_service_name" {
  value       = module.ecs.backend_service_name
  description = "The name of the backend ECS service"
}

output "frontend_service_name" {
  value       = module.ecs.frontend_service_name
  description = "The name of the frontend ECS service"
}

output "backend_task_definition_arn" {
  value       = module.ecs.backend_task_definition_arn
  description = "The ARN of the backend task definition"
}

output "frontend_task_definition_arn" {
  value       = module.ecs.frontend_task_definition_arn
  description = "The ARN of the frontend task definition"
}

