locals {
  identifier = var.identifier != "" ? var.identifier : "gym-${var.environment}-mysql"
}

resource "aws_db_instance" "main" {
  identifier             = local.identifier
  engine                 = "mysql"
  engine_version         = "8.4"
  instance_class         = var.instance_class
  allocated_storage      = 20
  max_allocated_storage  = 0
  storage_type           = "gp2"
  db_name                = var.db_name
  username               = var.username
  password               = var.password
  port                   = 3306
  db_subnet_group_name   = var.db_subnet_group_name
  vpc_security_group_ids = var.vpc_security_group_ids

  multi_az                     = false
  publicly_accessible          = false
  backup_retention_period      = 1
  performance_insights_enabled = false
  skip_final_snapshot          = true
  deletion_protection          = false
  apply_immediately            = true

  tags = merge(
    var.tags,
    {
      Name        = local.identifier
      Environment = var.environment
    }
  )

  lifecycle {
    ignore_changes = [password]
  }
}
