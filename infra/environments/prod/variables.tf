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
  default     = "gym_db_secret_pass_123"
  sensitive   = true
  description = "Master password for production RDS database"
}

variable "jwt_secret" {
  type        = string
  default     = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
  sensitive   = true
  description = "JWT Secret key for Spring Boot authentication"
}
