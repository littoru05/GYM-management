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

# Module: ECR Repositories for Backend and Frontend
module "ecr" {
  source = "../../modules/ecr"

  backend_repository_name  = "gym-backend"
  frontend_repository_name = "gym-frontend"
  max_image_count          = 5

  tags = {
    Environment = var.environment
    Module      = "ecr"
  }
}

# Module: Networking (VPC, Public Subnets, Internet Gateway, Security Groups)
module "networking" {
  source = "../../modules/networking"

  environment = var.environment

  tags = {
    Environment = var.environment
    Module      = "networking"
  }
}

# Module: Application Load Balancer
module "alb" {
  source = "../../modules/alb"

  alb_name        = "gym-${var.environment}-alb"
  environment     = var.environment
  vpc_id          = module.networking.vpc_id
  subnets         = module.networking.public_subnet_ids
  security_groups = [module.networking.alb_security_group_id]

  tags = {
    Environment = var.environment
    Module      = "alb"
  }
}

# Module: ECS Fargate Services (Backend + MySQL Sidecar, Frontend)
module "ecs" {
  source = "../../modules/ecs"

  environment               = var.environment
  aws_region                = var.aws_region
  vpc_id                    = module.networking.vpc_id
  public_subnet_ids         = module.networking.public_subnet_ids
  ecs_security_group_id     = module.networking.ecs_security_group_id
  backend_target_group_arn  = module.alb.backend_target_group_arn
  frontend_target_group_arn = module.alb.frontend_target_group_arn
  backend_image             = "${module.ecr.backend_repository_url}:latest"
  frontend_image            = "${module.ecr.frontend_repository_url}:latest"

  tags = {
    Environment = var.environment
    Module      = "ecs"
  }
}



