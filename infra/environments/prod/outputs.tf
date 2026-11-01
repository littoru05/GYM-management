output "github_actions_role_arn" {
  value       = module.ci_oidc.role_arn
  description = "The ARN of the IAM role for GitHub Actions to assume via OIDC"
}

output "ecr_backend_repository_url" {
  value       = data.aws_ecr_repository.backend.repository_url
  description = "The URL of the backend ECR repository"
}

output "ecr_frontend_repository_url" {
  value       = data.aws_ecr_repository.frontend.repository_url
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

output "database_subnet_ids" {
  value       = module.networking.database_subnet_ids
  description = "The IDs of the isolated database subnets"
}

output "alb_security_group_id" {
  value       = module.networking.alb_security_group_id
  description = "The security group ID of the ALB"
}

output "ecs_security_group_id" {
  value       = module.networking.ecs_security_group_id
  description = "The security group ID of the ECS tasks"
}

output "database_security_group_id" {
  value       = module.networking.database_security_group_id
  description = "The security group ID of the database subnets"
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

output "rds_instance_id" {
  value       = module.rds.db_instance_id
  description = "The RDS DB instance ID"
}

output "rds_instance_identifier" {
  value       = module.rds.db_instance_identifier
  description = "The RDS DB instance identifier"
}

output "rds_endpoint" {
  value       = module.rds.db_instance_endpoint
  description = "The connection endpoint for the RDS instance (address:port)"
}

output "rds_address" {
  value       = module.rds.db_instance_address
  description = "The hostname of the RDS instance"
}

output "rds_port" {
  value       = module.rds.db_instance_port
  description = "The port of the RDS instance"
}

output "cloudwatch_backend_log_group_name" {
  value       = module.ecs.cloudwatch_backend_log_group_name
  description = "The name of the backend CloudWatch log group"
}

output "cloudwatch_backend_log_group_arn" {
  value       = module.ecs.cloudwatch_backend_log_group_arn
  description = "The ARN of the backend CloudWatch log group"
}

output "cloudwatch_frontend_log_group_name" {
  value       = module.ecs.cloudwatch_frontend_log_group_name
  description = "The name of the frontend CloudWatch log group"
}

output "cloudwatch_frontend_log_group_arn" {
  value       = module.ecs.cloudwatch_frontend_log_group_arn
  description = "The ARN of the frontend CloudWatch log group"
}

output "cloudwatch_log_groups" {
  value       = module.ecs.cloudwatch_log_groups
  description = "Consolidated map of CloudWatch log groups by service name"
}
