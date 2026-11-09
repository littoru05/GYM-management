variable "aws_region" {
  type        = string
  default     = "ap-southeast-1"
  description = "AWS region for production infrastructure"
}

variable "environment" {
  type        = string
  default     = "prod"
  description = "Deployment environment name"
}

variable "github_repo" {
  type        = string
  default     = "littoru05/GYM-management"
  description = "GitHub repository in 'owner/repo' format"
}

variable "log_retention_in_days" {
  type        = number
  default     = 7
  description = "Retention period in days for CloudWatch log groups in production"
}

variable "db_password" {
  type        = string
  sensitive   = true
  description = "Master password for production RDS database (supplied via TF_VAR_db_password or AWS SSM Parameter Store)"
}

variable "jwt_secret" {
  type        = string
  sensitive   = true
  description = "JWT Secret key for Spring Boot authentication (supplied via TF_VAR_jwt_secret or AWS SSM Parameter Store)"
}
