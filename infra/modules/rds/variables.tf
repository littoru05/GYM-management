variable "environment" {
  type        = string
  default     = "prod"
  description = "Deployment environment name"
}

variable "identifier" {
  type        = string
  default     = ""
  description = "RDS DB instance identifier (defaults to gym-<env>-mysql if empty)"
}

variable "instance_class" {
  type        = string
  default     = "db.t4g.micro"
  description = "Instance class for RDS instance (Free Tier eligible db.t4g.micro or db.t3.micro)"

  validation {
    condition     = contains(["db.t4g.micro", "db.t3.micro"], var.instance_class)
    error_message = "RDS instance_class must be Free Tier eligible (db.t4g.micro or db.t3.micro)."
  }
}

variable "db_name" {
  type        = string
  default     = "gym_management"
  description = "Initial database name to create"
}

variable "username" {
  type        = string
  default     = "gym_user"
  description = "Master database username"
}

variable "password" {
  type        = string
  sensitive   = true
  description = "Master database password"
}

variable "db_subnet_group_name" {
  type        = string
  description = "Name of the DB subnet group in private isolated subnets"
}

variable "vpc_security_group_ids" {
  type        = list(string)
  description = "List of security group IDs granting access to MySQL"
}

variable "tags" {
  type        = map(string)
  default     = {}
  description = "Tags to attach to RDS resources"
}
