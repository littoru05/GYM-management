output "cluster_id" {
  value       = aws_ecs_cluster.main.id
  description = "The ID of the ECS cluster"
}

output "cluster_name" {
  value       = aws_ecs_cluster.main.name
  description = "The name of the ECS cluster"
}

output "backend_service_name" {
  value       = aws_ecs_service.backend.name
  description = "The name of the backend ECS service"
}

output "frontend_service_name" {
  value       = aws_ecs_service.frontend.name
  description = "The name of the frontend ECS service"
}

output "backend_task_definition_arn" {
  value       = aws_ecs_task_definition.backend.arn
  description = "The ARN of the backend task definition"
}

output "frontend_task_definition_arn" {
  value       = aws_ecs_task_definition.frontend.arn
  description = "The ARN of the frontend task definition"
}

output "db_password_ssm_arn" {
  value       = aws_ssm_parameter.db_password.arn
  description = "The ARN of the database password SSM parameter"
}

output "jwt_secret_ssm_arn" {
  value       = aws_ssm_parameter.jwt_secret.arn
  description = "The ARN of the JWT secret SSM parameter"
}
