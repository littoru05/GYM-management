# --- SSM Parameter for Billing Alert Emails ---
resource "aws_ssm_parameter" "billing_alert_emails" {
  name        = "/gym/${var.environment}/billing_alert_emails"
  description = "Comma-separated list of email addresses for AWS billing alerts"
  type        = "String"
  value       = var.default_alert_email

  tags = var.tags

  lifecycle {
    ignore_changes = [value]
  }
}

locals {
  # Parse comma-separated emails from SSM parameter
  alert_emails = [
    for email in split(",", aws_ssm_parameter.billing_alert_emails.value) : trimspace(email)
    if trimspace(email) != ""
  ]
}

# --- AWS Budgets: Monthly Cost Control ---
resource "aws_budgets_budget" "monthly_cost" {
  name         = "gym-${var.environment}-monthly-budget"
  budget_type  = "COST"
  limit_amount = var.budget_limit_amount
  limit_unit   = "USD"
  time_unit    = "MONTHLY"

  # Trigger 1: 50% actual ($5)
  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                  = 50
    threshold_type             = "PERCENTAGE"
    notification_type          = "ACTUAL"
    subscriber_email_addresses = local.alert_emails
  }

  # Trigger 2: 80% actual ($8)
  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                  = 80
    threshold_type             = "PERCENTAGE"
    notification_type          = "ACTUAL"
    subscriber_email_addresses = local.alert_emails
  }

  # Trigger 3: 100% forecasted ($10)
  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                  = 100
    threshold_type             = "PERCENTAGE"
    notification_type          = "FORECASTED"
    subscriber_email_addresses = local.alert_emails
  }

  tags = var.tags
}
