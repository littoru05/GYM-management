variable "github_repo" {
  type        = string
  description = "GitHub repository in 'owner/repo' format (e.g., 'littoru05/GYM-management')"
}

variable "role_name" {
  type        = string
  default     = "gym-management-github-actions-role"
  description = "IAM Role name assumed by GitHub Actions via OIDC"
}

variable "tags" {
  type        = map(string)
  default     = {}
  description = "Tags to apply to resources"
}
