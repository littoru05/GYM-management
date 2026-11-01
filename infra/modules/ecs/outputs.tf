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

output "cloudwatch_backend_log_group_name" {
  value       = aws_cloudwatch_log_group.backend.name
  description = "The name of the backend CloudWatch log group"
}

output "cloudwatch_backend_log_group_arn" {
  value       = aws_cloudwatch_log_group.backend.arn
  description = "The ARN of the backend CloudWatch log group"
}

output "cloudwatch_mysql_log_group_name" {
  value       = aws_cloudwatch_log_group.mysql.name
  description = "The name of the MySQL CloudWatch log group"
}

output "cloudwatch_mysql_log_group_arn" {
  value       = aws_cloudwatch_log_group.mysql.arn
  description = "The ARN of the MySQL CloudWatch log group"
}

output "cloudwatch_frontend_log_group_name" {
  value       = aws_cloudwatch_log_group.frontend.name
  description = "The name of the frontend CloudWatch log group"
}

output "cloudwatch_frontend_log_group_arn" {
  value       = aws_cloudwatch_log_group.frontend.arn
  description = "The ARN of the frontend CloudWatch log group"
}


output "cloudwatch_log_groups" {
  value = {
    backend = {
      name = aws_cloudwatch_log_group.backend.name
      arn  = aws_cloudwatch_log_group.backend.arn
    }
    mysql = {
      name = aws_cloudwatch_log_group.mysql.name
      arn  = aws_cloudwatch_log_group.mysql.arn
    }
    frontend = {
      name = aws_cloudwatch_log_group.frontend.name
      arn  = aws_cloudwatch_log_group.frontend.arn
    }
  }
  description = "Consolidated map of CloudWatch log groups by service name"
}
