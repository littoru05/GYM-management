locals {
  identifier = var.identifier != "" ? var.identifier : "gym-${var.environment}-mysql"
}

resource "aws_db_instance" "main" {
  identifier             = local.identifier
  engine                 = var.engine
  engine_version         = var.engine_version
  instance_class         = var.instance_class
  allocated_storage      = var.allocated_storage
  max_allocated_storage  = var.max_allocated_storage
  storage_type           = var.storage_type
  db_name                = var.db_name
  username               = var.username
  password               = var.password
  port                   = var.port
  db_subnet_group_name   = var.db_subnet_group_name
  vpc_security_group_ids = var.vpc_security_group_ids

  multi_az                     = var.multi_az
  publicly_accessible          = var.publicly_accessible
  backup_retention_period      = var.backup_retention_period
  performance_insights_enabled = var.performance_insights_enabled
  skip_final_snapshot          = var.skip_final_snapshot
  deletion_protection          = var.deletion_protection
  apply_immediately            = var.apply_immediately

  tags = merge(
    var.tags,
    {
      Name        = local.identifier
      Environment = var.environment
    }
  )
}
