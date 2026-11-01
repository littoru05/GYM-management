variable "vpc_cidr" {
  type        = string
  default     = "10.0.0.0/16"
  description = "CIDR block for the VPC"
}

variable "environment" {
  type        = string
  default     = "staging"
  description = "Deployment environment name"
}

variable "public_subnet_cidrs" {
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
  description = "CIDR blocks for the public subnets (at least 2 in different AZs for ALB)"
}

variable "availability_zones" {
  type        = list(string)
  default     = []
  description = "Availability zones to use for subnets (defaults to first available AZs in region)"
}

variable "enable_database_subnets" {
  type        = bool
  default     = false
  description = "Whether to provision isolated private database subnets and database security group"
}

variable "database_subnet_cidrs" {
  type        = list(string)
  default     = ["10.0.10.0/24", "10.0.11.0/24"]
  description = "CIDR blocks for isolated database subnets (at least 2 in different AZs for RDS Subnet Group)"
}

variable "tags" {
  type        = map(string)
  default     = {}
  description = "Tags to attach to networking resources"
}
