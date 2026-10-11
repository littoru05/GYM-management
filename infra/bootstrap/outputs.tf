output "state_bucket_name" {
  value       = aws_s3_bucket.state_bucket.id
  description = "The S3 bucket for Terraform remote state"
}

output "dynamodb_table_name" {
  value       = aws_dynamodb_table.state_locks.name
  description = "The DynamoDB table for state locking"
}
