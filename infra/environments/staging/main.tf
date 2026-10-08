terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "GYM-management"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}

# Module: CI/CD GitHub Actions OIDC Authentication
module "ci_oidc" {
  source = "../../modules/ci-oidc"

  github_repo = var.github_repo
  role_name   = "gym-management-${var.environment}-github-actions-role"

  tags = {
    Environment = var.environment
    Module      = "ci-oidc"
  }
}
