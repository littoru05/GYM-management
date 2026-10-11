variable "environment" {
  type        = string
  default     = "staging"
  description = "Deployment environment name"
}

variable "aws_region" {
  type        = string
  default     = "ap-southeast-1"
  description = "AWS Region for deployment"
}

variable "cluster_name" {
  type        = string
  default     = ""
  description = "ECS cluster name (defaults to gym-<env>-cluster if empty)"
}

variable "vpc_id" {
  type        = string
  description = "The VPC ID"
}

variable "public_subnet_ids" {
  type        = list(string)
  description = "Public subnet IDs to run ECS Fargate tasks in"
}

variable "ecs_security_group_id" {
  type        = string
  description = "Security group ID for ECS tasks"
}

variable "backend_target_group_arn" {
  type        = string
  description = "ARN of the ALB target group for backend"
}

variable "frontend_target_group_arn" {
  type        = string
  description = "ARN of the ALB target group for frontend"
}

variable "backend_image" {
  type        = string
  description = "ECR image URI for backend container"
}

variable "frontend_image" {
  type        = string
  description = "ECR image URI for frontend container"
}

variable "backend_cpu" {
  type        = number
  default     = 1024
  description = "Total CPU units for backend task (Spring Boot + MySQL sidecar)"
}

variable "backend_memory" {
  type        = number
  default     = 2048
  description = "Total memory (in MiB) for backend task (Spring Boot + MySQL sidecar)"
}

variable "frontend_cpu" {
  type        = number
  default     = 256
  description = "CPU units for frontend task"
}

variable "frontend_memory" {
  type        = number
  default     = 512
  description = "Memory (in MiB) for frontend task"
}

variable "backend_desired_count" {
  type        = number
  default     = 1
  description = "Desired number of backend tasks running"
}

variable "frontend_desired_count" {
  type        = number
  default     = 1
  description = "Desired number of frontend tasks running"
}

variable "db_password" {
  type        = string
  default     = "gym_db_secret_pass_123"
  sensitive   = true
  description = "Database password to store in SSM Parameter Store"
}

variable "jwt_secret" {
  type        = string
  default     = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
  sensitive   = true
  description = "JWT Secret key to store in SSM Parameter Store"
}

variable "tags" {
  type        = map(string)
  default     = {}
  description = "Tags to assign to ECS resources"
}
