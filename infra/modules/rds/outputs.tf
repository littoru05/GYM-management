output "db_instance_id" {
  value       = aws_db_instance.main.id
  description = "The RDS instance ID"
}

output "db_instance_identifier" {
  value       = aws_db_instance.main.identifier
  description = "The RDS instance identifier"
}

output "db_instance_address" {
  value       = aws_db_instance.main.address
  description = "The hostname of the RDS database instance"
}

output "db_instance_endpoint" {
  value       = aws_db_instance.main.endpoint
  description = "The connection endpoint of the RDS database instance (address:port)"
}

output "db_instance_port" {
  value       = aws_db_instance.main.port
  description = "The database port"
}

output "db_instance_arn" {
  value       = aws_db_instance.main.arn
  description = "The ARN of the RDS database instance"
}

output "db_name" {
  value       = aws_db_instance.main.db_name
  description = "The database name"
}

output "db_username" {
  value       = aws_db_instance.main.username
  description = "The master username for the database"
}
