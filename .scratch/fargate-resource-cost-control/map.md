# Fargate Resource Tuning and Cost Control

This effort tunes ECS Fargate task resources and sets up automated AWS cost monitoring and budget alarms for staging.

## Tickets

- [01-task-def-tuning-and-billing-alarm.md](./issues/01-task-def-tuning-and-billing-alarm.md)
- [02-cloudwatch-centralized-logging.md](./issues/02-cloudwatch-centralized-logging.md)
- [03-aws-monitoring-and-billing-report.md](./issues/03-aws-monitoring-and-billing-report.md)

## Decisions so far
- Task Definition Backend sizing reduced to 512 vCPU units (0.5 vCPU) and 2048 MiB RAM, with container soft memory reservations (MySQL: 768 MiB, Spring Boot: 1024 MiB) to avoid idle resource waste and OOM crashes ([01-task-def-tuning-and-billing-alarm.md](./issues/01-task-def-tuning-and-billing-alarm.md)).
- Module `cost-monitoring` provisions an AWS Budget capped at $10.00/month (USD) with 3 alert triggers: 50% actual ($5), 80% actual ($8), and 100% forecasted ($10) ([01-task-def-tuning-and-billing-alarm.md](./issues/01-task-def-tuning-and-billing-alarm.md)).
- Notification recipient emails are managed via SSM Parameter Store (`/gym/${environment}/billing_alert_emails`) with `lifecycle { ignore_changes = [value] }` to prevent Terraform from overwriting sensitive email addresses updated via AWS Console/CLI ([01-task-def-tuning-and-billing-alarm.md](./issues/01-task-def-tuning-and-billing-alarm.md)).
- Terraform uses a `data "aws_ssm_parameter"` data source to read the live parameter value at plan/apply time, ensuring out-of-band email updates in SSM propagate to AWS Budget alert subscribers upon each deployment ([01-task-def-tuning-and-billing-alarm.md](./issues/01-task-def-tuning-and-billing-alarm.md)).
- AWS Budget notification blocks refactored with `dynamic "notification"` to eliminate code duplication.
- Parameterized CloudWatch retention period (`log_retention_in_days = 7`) across all ECS log groups and configured `SPRING_OUTPUT_ANSI_ENABLED=NEVER` to guarantee clean realtime CloudWatch log streams ([02-cloudwatch-centralized-logging.md](./issues/02-cloudwatch-centralized-logging.md)).
- Generated comprehensive AWS Resource Monitoring & Operational Report at `docs/Bao_cao_giam_sat_tai_nguyen_AWS.pdf` backed by real CloudWatch metrics and Cost Explorer billing data, accompanied by an automated report generator script at `scripts/generate-aws-monitoring-report.py` ([03-aws-monitoring-and-billing-report.md](./issues/03-aws-monitoring-and-billing-report.md)).
