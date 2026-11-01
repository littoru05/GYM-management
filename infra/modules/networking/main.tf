data "aws_availability_zones" "available" {
  state = "available"
}

locals {
  max_subnets = max(length(var.public_subnet_cidrs), length(var.database_subnet_cidrs))
  azs         = length(var.availability_zones) > 0 ? var.availability_zones : slice(data.aws_availability_zones.available.names, 0, local.max_subnets)
}

# Virtual Private Cloud (VPC)
resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = merge(
    var.tags,
    {
      Name = "gym-${var.environment}-vpc"
    }
  )
}

# Internet Gateway for public internet access
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = merge(
    var.tags,
    {
      Name = "gym-${var.environment}-igw"
    }
  )
}

# Public Subnets (at least 2 in distinct AZs for ALB requirement)
resource "aws_subnet" "public" {
  count                   = length(var.public_subnet_cidrs)
  vpc_id                  = aws_vpc.main.id
  cidr_block              = var.public_subnet_cidrs[count.index]
  availability_zone       = local.azs[count.index]
  map_public_ip_on_launch = true

  tags = merge(
    var.tags,
    {
      Name = "gym-${var.environment}-public-subnet-${count.index + 1}"
    }
  )
}

# Public Route Table routing 0.0.0.0/0 to Internet Gateway
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = merge(
    var.tags,
    {
      Name = "gym-${var.environment}-public-rt"
    }
  )
}

# Route Table Associations for Public Subnets
resource "aws_route_table_association" "public" {
  count          = length(var.public_subnet_cidrs)
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

# Security Group for Application Load Balancer
resource "aws_security_group" "alb" {
  name        = "gym-${var.environment}-alb-sg"
  description = "Security group for Application Load Balancer"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "Allow inbound HTTP from internet"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    description = "Allow all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(
    var.tags,
    {
      Name = "gym-${var.environment}-alb-sg"
    }
  )
}

# Security Group for ECS Tasks (Backend & Frontend)
resource "aws_security_group" "ecs" {
  name        = "gym-${var.environment}-ecs-sg"
  description = "Security group for ECS Fargate tasks"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "Allow backend HTTP traffic from ALB on port 8080"
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  ingress {
    description     = "Allow frontend HTTP traffic from ALB on port 80"
    from_port       = 80
    to_port         = 80
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  egress {
    description = "Allow all outbound traffic (ECR pull, SSM, DB)"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(
    var.tags,
    {
      Name = "gym-${var.environment}-ecs-sg"
    }
  )
}

# Isolated Private Database Subnets (no internet gateway or NAT attached)
resource "aws_subnet" "database" {
  count                   = var.enable_database_subnets ? length(var.database_subnet_cidrs) : 0
  vpc_id                  = aws_vpc.main.id
  cidr_block              = var.database_subnet_cidrs[count.index]
  availability_zone       = local.azs[count.index]
  map_public_ip_on_launch = false

  tags = merge(
    var.tags,
    {
      Name = "gym-${var.environment}-db-subnet-${count.index + 1}"
    }
  )
}

# Isolated Route Table for Database Subnets (Internal-only, no internet route)
resource "aws_route_table" "database" {
  count  = var.enable_database_subnets ? 1 : 0
  vpc_id = aws_vpc.main.id

  tags = merge(
    var.tags,
    {
      Name = "gym-${var.environment}-db-rt"
    }
  )
}

# Route Table Associations for Database Subnets
resource "aws_route_table_association" "database" {
  count          = var.enable_database_subnets ? length(var.database_subnet_cidrs) : 0
  subnet_id      = aws_subnet.database[count.index].id
  route_table_id = aws_route_table.database[0].id
}

# Database Subnet Group for RDS
resource "aws_db_subnet_group" "database" {
  count       = var.enable_database_subnets ? 1 : 0
  name        = "gym-${var.environment}-db-subnet-group"
  subnet_ids  = aws_subnet.database[*].id
  description = "Isolated database subnet group for gym ${var.environment}"

  tags = merge(
    var.tags,
    {
      Name = "gym-${var.environment}-db-subnet-group"
    }
  )
}

# Security Group for RDS MySQL Database (permits inbound port 3306 exclusively from ECS tasks)
resource "aws_security_group" "database" {
  count       = var.enable_database_subnets ? 1 : 0
  name        = "gym-${var.environment}-db-sg"
  description = "Security group for RDS MySQL database"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "Allow inbound MySQL from ECS tasks"
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs.id]
  }

  egress {
    description = "Allow all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(
    var.tags,
    {
      Name = "gym-${var.environment}-db-sg"
    }
  )
}
