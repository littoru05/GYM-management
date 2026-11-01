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

# Read live value from SSM Parameter Store at plan/apply time
# This ensures out-of-band updates in AWS Console/CLI are picked up by AWS Budget on next terraform apply
data "aws_ssm_parameter" "billing_alert_emails" {
  name       = aws_ssm_parameter.billing_alert_emails.name
  depends_on = [aws_ssm_parameter.billing_alert_emails]
}

locals {
  # Parse comma-separated emails from live SSM parameter
  raw_emails = split(",", coalesce(data.aws_ssm_parameter.billing_alert_emails.value, var.default_alert_email))
  alert_emails = [
    for email in local.raw_emails : trimspace(email)
    if trimspace(email) != ""
  ]

  # Threshold triggers: 50% actual ($5), 80% actual ($8), 100% forecasted ($10)
  budget_notifications = [
    {
      threshold         = 50
      notification_type = "ACTUAL"
    },
    {
      threshold         = 80
      notification_type = "ACTUAL"
    },
    {
      threshold         = 100
      notification_type = "FORECASTED"
    }
  ]
}

# --- AWS Budgets: Monthly Cost Control ---
resource "aws_budgets_budget" "monthly_cost" {
  name         = "gym-${var.environment}-monthly-budget"
  budget_type  = "COST"
  limit_amount = var.budget_limit_amount
  limit_unit   = "USD"
  time_unit    = "MONTHLY"

  dynamic "notification" {
    for_each = local.budget_notifications
    content {
      comparison_operator        = "GREATER_THAN"
      threshold                  = notification.value.threshold
      threshold_type             = "PERCENTAGE"
      notification_type          = notification.value.notification_type
      subscriber_email_addresses = local.alert_emails
    }
  }

  tags = var.tags
}
