locals {
  cluster_name = var.cluster_name != "" ? var.cluster_name : "gym-${var.environment}-cluster"

  mysql_log_group_name = length(aws_cloudwatch_log_group.mysql) > 0 ? aws_cloudwatch_log_group.mysql[0].name : "/ecs/gym-${var.environment}-mysql"

  db_mode_config = var.enable_mysql_sidecar ? {
    db_pool_size                      = "20"
    db_min_idle                       = "5"
    health_check_grace_period_seconds = 60
    } : {
    db_pool_size                      = "10"
    db_min_idle                       = "2"
    health_check_grace_period_seconds = 120
  }

  mysql_container = {
    name              = "mysql"
    image             = "mysql:8.4"
    essential         = true
    memoryReservation = var.mysql_container_memory_reservation

    portMappings = [
      {
        containerPort = 3306
        hostPort      = 3306
        protocol      = "tcp"
      }
    ]

    environment = [
      {
        name  = "MYSQL_DATABASE"
        value = var.db_name
      }
    ]

    secrets = [
      {
        name      = "MYSQL_ROOT_PASSWORD"
        valueFrom = aws_ssm_parameter.db_password.arn
      }
    ]

    healthCheck = {
      command     = ["CMD-SHELL", "mysqladmin ping -h 127.0.0.1 -uroot -p$$MYSQL_ROOT_PASSWORD --silent"]
      interval    = 10
      timeout     = 5
      retries     = 10
      startPeriod = 30
    }

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = local.mysql_log_group_name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "mysql"
      }
    }
  }

  backend_base_container = {
    name              = "backend"
    image             = var.backend_image
    essential         = true
    memoryReservation = var.backend_container_memory_reservation

    portMappings = [
      {
        containerPort = 8080
        hostPort      = 8080
        protocol      = "tcp"
      }
    ]

    environment = [
      {
        name  = "SPRING_PROFILES_ACTIVE"
        value = var.spring_profile
      },
      {
        name  = "SPRING_OUTPUT_ANSI_ENABLED"
        value = "NEVER"
      },
      {
        name  = "DB_HOST"
        value = var.db_host
      },
      {
        name  = "DB_PORT"
        value = tostring(var.db_port)
      },
      {
        name  = "DB_NAME"
        value = var.db_name
      },
      {
        name  = "DB_USERNAME"
        value = var.db_username
      },
      {
        name  = "DB_POOL_SIZE"
        value = local.db_mode_config.db_pool_size
      },
      {
        name  = "DB_MIN_IDLE"
        value = local.db_mode_config.db_min_idle
      },
      {
        name  = "SERVER_PORT"
        value = "8080"
      }
    ]

    secrets = [
      {
        name      = "DB_PASSWORD"
        valueFrom = aws_ssm_parameter.db_password.arn
      },
      {
        name      = "JWT_SECRET"
        valueFrom = aws_ssm_parameter.jwt_secret.arn
      }
    ]

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.backend.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "backend"
      }
    }
  }

  backend_container = merge(
    local.backend_base_container,
    var.enable_mysql_sidecar ? {
      dependsOn = [
        {
          containerName = "mysql"
          condition     = "HEALTHY"
        }
      ]
    } : {}
  )

  backend_task_containers = concat(
    [for c in [local.mysql_container] : c if var.enable_mysql_sidecar],
    [local.backend_container]
  )
}

# --- SSM Parameters for Secrets ---
resource "aws_ssm_parameter" "db_password" {
  name        = "/gym/${var.environment}/db_password"
  description = "Database password for backend and MySQL sidecar"
  type        = "SecureString"
  value       = var.db_password

  tags = var.tags

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "jwt_secret" {
  name        = "/gym/${var.environment}/jwt_secret"
  description = "JWT secret for Spring Boot authentication"
  type        = "SecureString"
  value       = var.jwt_secret

  tags = var.tags

  lifecycle {
    ignore_changes = [value]
  }
}

# --- CloudWatch Log Groups ---
resource "aws_cloudwatch_log_group" "backend" {
  name              = "/ecs/gym-${var.environment}-backend"
  retention_in_days = var.log_retention_in_days

  tags = var.tags
}

resource "aws_cloudwatch_log_group" "mysql" {
  count             = var.enable_mysql_sidecar ? 1 : 0
  name              = "/ecs/gym-${var.environment}-mysql"
  retention_in_days = var.log_retention_in_days

  tags = var.tags
}

moved {
  from = aws_cloudwatch_log_group.mysql
  to   = aws_cloudwatch_log_group.mysql[0]
}

resource "aws_cloudwatch_log_group" "frontend" {
  name              = "/ecs/gym-${var.environment}-frontend"
  retention_in_days = var.log_retention_in_days

  tags = var.tags
}

# --- IAM Roles for ECS ---
data "aws_iam_policy_document" "ecs_tasks_assume_role" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "ecs_execution_role" {
  name               = "gym-${var.environment}-ecs-execution-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_assume_role.json

  tags = var.tags
}

resource "aws_iam_role_policy_attachment" "ecs_execution_standard" {
  role       = aws_iam_role.ecs_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Policy allowing execution role to read secrets from SSM Parameter Store
data "aws_iam_policy_document" "ecs_ssm_access" {
  statement {
    sid    = "SSMParameterAccess"
    effect = "Allow"
    actions = [
      "ssm:GetParameters",
      "ssm:GetParameter"
    ]
    resources = [
      aws_ssm_parameter.db_password.arn,
      aws_ssm_parameter.jwt_secret.arn
    ]
  }

  statement {
    sid    = "KMSDecryptAccess"
    effect = "Allow"
    actions = [
      "kms:Decrypt"
    ]
    resources = ["*"]
  }
}

resource "aws_iam_role_policy" "ecs_ssm_access" {
  name   = "gym-${var.environment}-ecs-ssm-access"
  role   = aws_iam_role.ecs_execution_role.id
  policy = data.aws_iam_policy_document.ecs_ssm_access.json
}

resource "aws_iam_role" "ecs_task_role" {
  name               = "gym-${var.environment}-ecs-task-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_assume_role.json

  tags = var.tags
}

# --- ECS Cluster ---
resource "aws_ecs_cluster" "main" {
  name = local.cluster_name

  tags = merge(
    var.tags,
    {
      Name = local.cluster_name
    }
  )
}

resource "aws_ecs_cluster_capacity_providers" "main" {
  cluster_name = aws_ecs_cluster.main.name

  capacity_providers = ["FARGATE", "FARGATE_SPOT"]

  default_capacity_provider_strategy {
    capacity_provider = "FARGATE_SPOT"
    weight            = 100
    base              = 0
  }
}

# --- Backend Task Definition (Spring Boot + Optional Ephemeral MySQL Sidecar) ---
resource "aws_ecs_task_definition" "backend" {
  family                   = "gym-${var.environment}-backend"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = tostring(var.backend_cpu)
  memory                   = tostring(var.backend_memory)
  execution_role_arn       = aws_iam_role.ecs_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode(local.backend_task_containers)

  tags = var.tags
}

# --- Frontend Task Definition (React SPA on Nginx) ---
resource "aws_ecs_task_definition" "frontend" {
  family                   = "gym-${var.environment}-frontend"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = tostring(var.frontend_cpu)
  memory                   = tostring(var.frontend_memory)
  execution_role_arn       = aws_iam_role.ecs_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "frontend"
      image     = var.frontend_image
      essential = true

      portMappings = [
        {
          containerPort = 80
          hostPort      = 80
          protocol      = "tcp"
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.frontend.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "frontend"
        }
      }
    }
  ])

  tags = var.tags
}

# --- ECS Services (Fargate Spot) ---
resource "aws_ecs_service" "backend" {
  name                              = "gym-${var.environment}-backend"
  cluster                           = aws_ecs_cluster.main.id
  task_definition                   = aws_ecs_task_definition.backend.arn
  desired_count                     = var.backend_desired_count
  health_check_grace_period_seconds = local.db_mode_config.health_check_grace_period_seconds

  capacity_provider_strategy {
    capacity_provider = "FARGATE_SPOT"
    weight            = 100
    base              = 0
  }

  network_configuration {
    subnets          = var.public_subnet_ids
    security_groups  = [var.ecs_security_group_id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = var.backend_target_group_arn
    container_name   = "backend"
    container_port   = 8080
  }

  tags = var.tags

  depends_on = [aws_iam_role_policy.ecs_ssm_access]

  lifecycle {
    ignore_changes = [task_definition, desired_count]
  }
}

resource "aws_ecs_service" "frontend" {
  name            = "gym-${var.environment}-frontend"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.frontend.arn
  desired_count   = var.frontend_desired_count

  capacity_provider_strategy {
    capacity_provider = "FARGATE_SPOT"
    weight            = 100
    base              = 0
  }

  network_configuration {
    subnets          = var.public_subnet_ids
    security_groups  = [var.ecs_security_group_id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = var.frontend_target_group_arn
    container_name   = "frontend"
    container_port   = 80
  }

  tags = var.tags

  lifecycle {
    ignore_changes = [task_definition, desired_count]
  }
}
