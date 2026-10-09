output "backend_repository_name" {
  value       = aws_ecr_repository.backend.name
  description = "Name of the backend ECR repository"
}

output "backend_repository_url" {
  value       = aws_ecr_repository.backend.repository_url
  description = "URL of the backend ECR repository"
}

output "backend_repository_arn" {
  value       = aws_ecr_repository.backend.arn
  description = "ARN of the backend ECR repository"
}

output "frontend_repository_name" {
  value       = aws_ecr_repository.frontend.name
  description = "Name of the frontend ECR repository"
}

output "frontend_repository_url" {
  value       = aws_ecr_repository.frontend.repository_url
  description = "URL of the frontend ECR repository"
}

output "frontend_repository_arn" {
  value       = aws_ecr_repository.frontend.arn
  description = "ARN of the frontend ECR repository"
}
