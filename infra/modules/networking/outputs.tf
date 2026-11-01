output "vpc_id" {
  value       = aws_vpc.main.id
  description = "The ID of the VPC"
}

output "vpc_cidr_block" {
  value       = aws_vpc.main.cidr_block
  description = "The CIDR block of the VPC"
}

output "public_subnet_ids" {
  value       = aws_subnet.public[*].id
  description = "List of public subnet IDs"
}

output "alb_security_group_id" {
  value       = aws_security_group.alb.id
  description = "Security group ID for the ALB"
}

output "ecs_security_group_id" {
  value       = aws_security_group.ecs.id
  description = "Security group ID for ECS tasks"
}

output "database_subnet_ids" {
  value       = var.enable_database_subnets ? aws_subnet.database[*].id : []
  description = "List of private isolated database subnet IDs"
}

output "database_subnet_group_name" {
  value       = var.enable_database_subnets ? aws_db_subnet_group.database[0].name : ""
  description = "The name of the database subnet group"
}

output "database_subnet_group_id" {
  value       = var.enable_database_subnets ? aws_db_subnet_group.database[0].id : ""
  description = "The ID of the database subnet group"
}

output "database_security_group_id" {
  value       = var.enable_database_subnets ? aws_security_group.database[0].id : ""
  description = "Security group ID for the RDS MySQL database"
}
