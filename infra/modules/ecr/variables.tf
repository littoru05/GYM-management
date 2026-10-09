variable "backend_repository_name" {
  type        = string
  default     = "gym-backend"
  description = "Name of the backend ECR repository"
}

variable "frontend_repository_name" {
  type        = string
  default     = "gym-frontend"
  description = "Name of the frontend ECR repository"
}

variable "image_tag_mutability" {
  type        = string
  default     = "MUTABLE"
  description = "The tag mutability setting for the repository (MUTABLE or IMMUTABLE)"
}

variable "max_image_count" {
  type        = number
  default     = 5
  description = "The maximum number of recent images to retain in each repository"
}

variable "tags" {
  type        = map(string)
  default     = {}
  description = "Tags to attach to the ECR repositories"
}
