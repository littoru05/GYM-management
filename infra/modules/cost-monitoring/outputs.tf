output "budget_name" {
  description = "The name of the AWS budget"
  value       = aws_budgets_budget.monthly_cost.name
}

output "budget_limit_amount" {
  description = "Monthly budget limit in USD"
  value       = aws_budgets_budget.monthly_cost.limit_amount
}

output "ssm_parameter_name" {
  description = "SSM Parameter name for billing alert emails"
  value       = aws_ssm_parameter.billing_alert_emails.name
}
