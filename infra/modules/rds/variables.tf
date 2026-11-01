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

variable "engine" {
  type        = string
  default     = "mysql"
  description = "Database engine"
}

variable "engine_version" {
  type        = string
  default     = "8.4"
  description = "MySQL database engine version (8.4 LTS for Free Tier)"
}

variable "instance_class" {
  type        = string
  default     = "db.t4g.micro"
  description = "Instance class for RDS instance (Free Tier eligible db.t4g.micro or db.t3.micro)"
}

variable "allocated_storage" {
  type        = number
  default     = 20
  description = "Allocated storage in GB (20 GB max for Free Tier coverage)"
}

variable "max_allocated_storage" {
  type        = number
  default     = 0
  description = "Maximum storage threshold in GB for autoscaling (0 disables autoscaling for 100% Free Tier protection)"
}

variable "storage_type" {
  type        = string
  default     = "gp2"
  description = "Storage type for RDS instance (gp2 is covered under AWS Free Tier)"
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

variable "port" {
  type        = number
  default     = 3306
  description = "Database port"
}

variable "db_subnet_group_name" {
  type        = string
  description = "Name of the DB subnet group in private isolated subnets"
}

variable "vpc_security_group_ids" {
  type        = list(string)
  description = "List of security group IDs granting access to MySQL"
}

variable "multi_az" {
  type        = bool
  default     = false
  description = "Whether to enable Multi-AZ deployment (must be false for Free Tier compliance)"
}

variable "publicly_accessible" {
  type        = bool
  default     = false
  description = "Whether the database is publicly accessible (must be false for private subnet isolation)"
}

variable "backup_retention_period" {
  type        = number
  default     = 7
  description = "Backup retention period in days (7 days included in Free Tier)"
}

variable "performance_insights_enabled" {
  type        = bool
  default     = false
  description = "Whether to enable Performance Insights (disabled to stay strictly within Free Tier)"
}

variable "skip_final_snapshot" {
  type        = bool
  default     = true
  description = "Whether to skip final snapshot when destroying instance"
}

variable "deletion_protection" {
  type        = bool
  default     = false
  description = "Whether to enable deletion protection"
}

variable "apply_immediately" {
  type        = bool
  default     = true
  description = "Whether to apply database modifications immediately"
}

variable "tags" {
  type        = map(string)
  default     = {}
  description = "Tags to attach to RDS resources"
}
