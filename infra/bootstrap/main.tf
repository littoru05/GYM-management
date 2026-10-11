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
}

variable "aws_region" {
  type        = string
  default     = "ap-southeast-1"
  description = "AWS region for terraform remote state"
}

variable "state_bucket_name" {
  type        = string
  default     = "gym-management-tfstate-staging"
  description = "S3 bucket name for storing terraform remote state"
}

variable "dynamodb_table_name" {
  type        = string
  default     = "gym-management-tflock-staging"
  description = "DynamoDB table name for state locking"
}

# S3 Bucket for Terraform Remote State
resource "aws_s3_bucket" "state_bucket" {
  bucket        = var.state_bucket_name
  force_destroy = false

  tags = {
    Project     = "GYM-management"
    Environment = "staging"
    ManagedBy   = "Terraform-Bootstrap"
  }
}

resource "aws_s3_bucket_versioning" "state_versioning" {
  bucket = aws_s3_bucket.state_bucket.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "state_encryption" {
  bucket = aws_s3_bucket.state_bucket.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "state_public_block" {
  bucket = aws_s3_bucket.state_bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# DynamoDB Table for Terraform State Locking
resource "aws_dynamodb_table" "state_locks" {
  name         = var.dynamodb_table_name
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "LockID"

  attribute {
    name = "LockID"
    type = "S"
  }

  tags = {
    Project     = "GYM-management"
    Environment = "staging"
    ManagedBy   = "Terraform-Bootstrap"
  }
}
