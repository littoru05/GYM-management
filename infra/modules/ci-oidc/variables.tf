variable "github_repo" {
  type        = string
  description = "GitHub repository in 'owner/repo' format (e.g., 'littoru05/GYM-management')"
}

variable "role_name" {
  type        = string
  default     = "gym-management-github-actions-role"
  description = "IAM Role name assumed by GitHub Actions via OIDC"
}

variable "create_oidc_provider" {
  type        = bool
  default     = true
  description = "Whether to create the account-wide GitHub OIDC provider (false uses data lookup for existing provider)"
}

variable "oidc_provider_arn" {
  type        = string
  default     = ""
  description = "Existing GitHub OIDC provider ARN (optional; auto-resolved from account ID if empty and create_oidc_provider is false)"
}

variable "tags" {
  type        = map(string)
  default     = {}
  description = "Tags to apply to resources"
}
