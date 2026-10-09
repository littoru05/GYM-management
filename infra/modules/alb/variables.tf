variable "alb_name" {
  type        = string
  default     = "gym-staging-alb"
  description = "Name of the Application Load Balancer"
}

variable "environment" {
  type        = string
  default     = "staging"
  description = "Deployment environment name"
}

variable "vpc_id" {
  type        = string
  description = "The VPC ID where target groups are located"
}

variable "subnets" {
  type        = list(string)
  description = "List of public subnet IDs to attach the ALB"
}

variable "security_groups" {
  type        = list(string)
  description = "List of security group IDs for the ALB"
}

variable "backend_port" {
  type        = number
  default     = 8080
  description = "Port the backend application listens on"
}

variable "frontend_port" {
  type        = number
  default     = 80
  description = "Port the frontend application listens on"
}

variable "health_check_path_backend" {
  type        = string
  default     = "/api/health"
  description = "Health check path for the backend target group"
}

variable "health_check_path_frontend" {
  type        = string
  default     = "/"
  description = "Health check path for the frontend target group"
}

variable "tags" {
  type        = map(string)
  default     = {}
  description = "Tags to attach to ALB resources"
}
