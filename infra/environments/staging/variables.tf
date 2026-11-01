variable "aws_region" {
  type        = string
  default     = "ap-southeast-1"
  description = "AWS region for staging infrastructure"
}

variable "environment" {
  type        = string
  default     = "staging"
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
  description = "Retention period in days for CloudWatch log groups in staging"
}
