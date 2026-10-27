# Fargate Resource Tuning and Cost Control

This effort tunes ECS Fargate task resources and sets up automated AWS cost monitoring and budget alarms for staging.

## Tickets

- [01-task-def-tuning-and-billing-alarm.md](./issues/01-task-def-tuning-and-billing-alarm.md)

## Decisions so far
- Task Definition Backend sizing reduced to 512 vCPU units (0.5 vCPU) and 2048 MiB RAM, with container soft memory reservations (MySQL: 768 MiB, Spring Boot: 1024 MiB) to avoid idle resource waste and OOM crashes ([01-task-def-tuning-and-billing-alarm.md](./issues/01-task-def-tuning-and-billing-alarm.md)).
- Module `cost-monitoring` provisions an AWS Budget capped at $10.00/month (USD) with 3 alert triggers: 50% actual ($5), 80% actual ($8), and 100% forecasted ($10) ([01-task-def-tuning-and-billing-alarm.md](./issues/01-task-def-tuning-and-billing-alarm.md)).
- Notification recipient emails are managed via SSM Parameter Store (`/gym/${environment}/billing_alert_emails`) with `lifecycle { ignore_changes = [value] }` to prevent Terraform from overwriting sensitive email addresses updated via AWS Console/CLI ([01-task-def-tuning-and-billing-alarm.md](./issues/01-task-def-tuning-and-billing-alarm.md)).
- Terraform uses a `data "aws_ssm_parameter"` data source to read the live parameter value at plan/apply time, ensuring out-of-band email updates in SSM propagate to AWS Budget alert subscribers upon each deployment ([01-task-def-tuning-and-billing-alarm.md](./issues/01-task-def-tuning-and-billing-alarm.md)).
- AWS Budget notification blocks refactored with `dynamic "notification"` to eliminate code duplication.
