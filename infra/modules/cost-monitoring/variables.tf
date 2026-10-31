variable "environment" {
  type        = string
  default     = "staging"
  description = "Deployment environment name"
}

variable "budget_limit_amount" {
  type        = string
  default     = "10"
  description = "Monthly budget limit in USD"
}

variable "default_alert_email" {
  type        = string
  default     = "admin@example.com"
  description = "Default/fallback email for billing alerts when SSM parameter is unpopulated"
}

variable "tags" {
  type        = map(string)
  default     = {}
  description = "Tags to assign to resources"
}
