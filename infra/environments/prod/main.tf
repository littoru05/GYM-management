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

# Reference shared account-level singleton ECR repositories via data lookups
data "aws_ecr_repository" "backend" {
  name = "gym-backend"
}

data "aws_ecr_repository" "frontend" {
  name = "gym-frontend"
}

# Module: CI/CD GitHub Actions OIDC Authentication (reusing account OIDC provider)
module "ci_oidc" {
  source = "../../modules/ci-oidc"

  github_repo          = var.github_repo
  role_name            = "gym-management-${var.environment}-github-actions-role"
  create_oidc_provider = false

  tags = {
    Environment = var.environment
    Module      = "ci-oidc"
  }
}

# Module: Networking (VPC 10.1.0.0/16, Public Subnets, Isolated Database Subnets)
module "networking" {
  source = "../../modules/networking"

  environment             = var.environment
  vpc_cidr                = "10.1.0.0/16"
  public_subnet_cidrs     = ["10.1.1.0/24", "10.1.2.0/24"]
  enable_database_subnets = true
  database_subnet_cidrs   = ["10.1.10.0/24", "10.1.11.0/24"]

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

# Module: Single-AZ Free-Tier RDS MySQL
module "rds" {
  source = "../../modules/rds"

  environment            = var.environment
  identifier             = "gym-${var.environment}-mysql"
  db_name                = "gym_management"
  username               = "gym_user"
  password               = var.db_password
  db_subnet_group_name   = module.networking.database_subnet_group_name
  vpc_security_group_ids = [module.networking.database_security_group_id]

  tags = {
    Environment = var.environment
    Module      = "rds"
  }
}

# Module: ECS Fargate Services (Backend without sidecar, Frontend)
module "ecs" {
  source = "../../modules/ecs"

  environment               = var.environment
  aws_region                = var.aws_region
  cluster_name              = "gym-${var.environment}-cluster"
  vpc_id                    = module.networking.vpc_id
  public_subnet_ids         = module.networking.public_subnet_ids
  ecs_security_group_id     = module.networking.ecs_security_group_id
  backend_target_group_arn  = module.alb.backend_target_group_arn
  frontend_target_group_arn = module.alb.frontend_target_group_arn
  backend_image             = "${data.aws_ecr_repository.backend.repository_url}:prod-latest"
  frontend_image            = "${data.aws_ecr_repository.frontend.repository_url}:prod-latest"
  log_retention_in_days     = var.log_retention_in_days

  # Prod sizing & external RDS wiring
  enable_mysql_sidecar                 = false
  spring_profile                       = "prod"
  backend_cpu                          = 512
  backend_memory                       = 1024
  backend_container_memory_reservation = 1024
  frontend_cpu                         = 256
  frontend_memory                      = 512

  db_host     = module.rds.db_instance_address
  db_port     = module.rds.db_instance_port
  db_name     = module.rds.db_name
  db_username = module.rds.db_username
  db_password = var.db_password
  jwt_secret  = var.jwt_secret

  tags = {
    Environment = var.environment
    Module      = "ecs"
  }
}
