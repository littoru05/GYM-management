# Application Load Balancer
resource "aws_lb" "main" {
  name               = var.alb_name
  internal           = false
  load_balancer_type = "application"
  security_groups    = var.security_groups
  subnets            = var.subnets

  enable_deletion_protection = false

  tags = merge(
    var.tags,
    {
      Name = var.alb_name
    }
  )
}

# Backend Target Group (target_type = "ip" required for ECS Fargate awsvpc mode)
resource "aws_lb_target_group" "backend" {
  name        = "gym-${var.environment}-backend-tg"
  port        = var.backend_port
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "ip"

  health_check {
    enabled             = true
    path                = var.health_check_path_backend
    protocol            = "HTTP"
    port                = "traffic-port"
    interval            = 30
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
    matcher             = "200"
  }

  tags = merge(
    var.tags,
    {
      Name    = "gym-${var.environment}-backend-tg"
      Service = "backend"
    }
  )
}

# Frontend Target Group (target_type = "ip" required for ECS Fargate awsvpc mode)
resource "aws_lb_target_group" "frontend" {
  name        = "gym-${var.environment}-frontend-tg"
  port        = var.frontend_port
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "ip"

  health_check {
    enabled             = true
    path                = var.health_check_path_frontend
    protocol            = "HTTP"
    port                = "traffic-port"
    interval            = 30
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
    matcher             = "200"
  }

  tags = merge(
    var.tags,
    {
      Name    = "gym-${var.environment}-frontend-tg"
      Service = "frontend"
    }
  )
}

# HTTP Listener on port 80 - default action forwards to Frontend
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.main.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.frontend.arn
  }
}

# Path-based routing rule: /api and /api/* forwards to Backend Target Group
resource "aws_lb_listener_rule" "backend_api" {
  listener_arn = aws_lb_listener.http.arn
  priority     = 10

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.backend.arn
  }

  condition {
    path_pattern {
      values = ["/api/*", "/api"]
    }
  }
}
