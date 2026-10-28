# 02: Cấu hình Quản lý Log Tập trung qua AWS CloudWatch Logs

**Blocked by:** 01-task-def-tuning-and-billing-alarm.md

**Type:** task
**Status:** resolved

## Context
Nhằm đảm bảo khả năng quan sát (observability), gỡ lỗi thời gian thực và quản lý tập trung toàn bộ log của hệ thống GYM Management chạy trên AWS ECS Fargate mà vẫn tối ưu chi phí lưu trữ, cần:
1. Cấu hình log driver `awslogs` cho cả hai container trong Task Definition Backend (Spring Boot và MySQL 8.4 sidecar).
2. Thiết lập chính sách lưu trữ (Retention Policy) 7 ngày cho các CloudWatch Log Groups nhằm ngăn chặn chi phí lưu trữ tích tụ theo thời gian.
3. Tham số hóa biến cấu hình `log_retention_in_days` trong Terraform module ECS.
4. Cấu hình loại bỏ mã màu ANSI (`SPRING_OUTPUT_ANSI_ENABLED=NEVER`) cho container backend để log hiển thị sạch sẽ, dễ lọc và truy vấn trên AWS CloudWatch Logs Console/Insights.
5. Xuất outputs tên và ARN của các CloudWatch Log Groups để phục vụ tra cứu và tích hợp dashboard giám sát.

## Requirements
- **CloudWatch Log Groups**:
  - `aws_cloudwatch_log_group.backend`: name = `/ecs/gym-${var.environment}-backend`, `retention_in_days = var.log_retention_in_days`.
  - `aws_cloudwatch_log_group.mysql`: name = `/ecs/gym-${var.environment}-mysql`, `retention_in_days = var.log_retention_in_days`.
  - `aws_cloudwatch_log_group.frontend`: name = `/ecs/gym-${var.environment}-frontend`, `retention_in_days = var.log_retention_in_days`.
- **ECS Variables (`infra/modules/ecs/variables.tf`)**:
  - Bổ sung biến `log_retention_in_days` kiểu `number`, default = `7`, mô tả "Retention period in days for CloudWatch log groups".
- **Container Definitions (`infra/modules/ecs/main.tf`)**:
  - Container `mysql`:
    - `logConfiguration` sử dụng logDriver `awslogs`.
    - options: `awslogs-group`, `awslogs-region`, `awslogs-stream-prefix = "mysql"`.
  - Container `backend`:
    - `logConfiguration` sử dụng logDriver `awslogs`.
    - options: `awslogs-group`, `awslogs-region`, `awslogs-stream-prefix = "backend"`.
    - Environment variables: bổ sung `SPRING_OUTPUT_ANSI_ENABLED = "NEVER"`.
- **IAM Execution Role**:
  - Đảm bảo policy `AmazonECSTaskExecutionRolePolicy` được gán vào `ecs_execution_role` cho phép `logs:CreateLogStream` và `logs:PutLogEvents`.
- **Terraform Outputs**:
  - Xuất log group names và ARNs trong `infra/modules/ecs/outputs.tf` và `infra/environments/staging/outputs.tf`.
- **Testing**:
  - Tạo `infra/tests/test_cloudwatch_logging.py` kiểm thử toàn bộ các điều kiện trên.
- **DoD Verification**:
  - Toàn bộ log console hiển thị trực tiếp trên CloudWatch Log Streams theo thời gian thực (hỗ trợ theo dõi qua AWS CLI: `aws logs tail /ecs/gym-staging-backend --follow` và `aws logs tail /ecs/gym-staging-mysql --follow`).

## Answer
- **Tham số hóa Retention Policy**:
  - Bổ sung biến `log_retention_in_days` (mặc định = 7) vào `infra/modules/ecs/variables.tf`.
  - Cập nhật cả 3 CloudWatch Log Groups (`backend`, `mysql`, `frontend`) trong `infra/modules/ecs/main.tf` sử dụng `retention_in_days = var.log_retention_in_days`, đảm bảo chi phí lưu trữ log được khống chế theo đúng chu kỳ 7 ngày.
- **Cấu hình awslogs Driver & Clean Log Output**:
  - Cấu hình `logConfiguration` với `logDriver = "awslogs"` cho container `mysql` (prefix `mysql`) và container `backend` (prefix `backend`).
  - Bổ sung `SPRING_OUTPUT_ANSI_ENABLED = "NEVER"` vào biến môi trường của container backend trong Task Definition ECS Fargate để loại bỏ toàn bộ chuỗi ANSI escape, giúp CloudWatch Log Streams và Insights hiển thị văn bản thuần túy, sạch sẽ theo thời gian thực.
  - `ecs_execution_role` được gắn policy `AmazonECSTaskExecutionRolePolicy` cho phép tự động tạo log stream và ghi log events lên CloudWatch.
- **Terraform Outputs**:
  - Xuất outputs tên và ARN của các log groups (`cloudwatch_backend_log_group_name`, `cloudwatch_backend_log_group_arn`, `cloudwatch_mysql_log_group_name`, `cloudwatch_mysql_log_group_arn`, `cloudwatch_frontend_log_group_name`, `cloudwatch_frontend_log_group_arn`) trong `infra/modules/ecs/outputs.tf` và chuyển tiếp ra `infra/environments/staging/outputs.tf`.
- **Kiểm thử Tự động & DoD**:
  - Xây dựng bộ test `infra/tests/test_cloudwatch_logging.py` xác thực đầy đủ biến cấu hình, 7-day retention, awslogs driver, ANSI disabled, IAM log permissions và Terraform outputs.
  - Toàn bộ 38 bài test infra và `terraform validate` đều pass 100%.
  - Cung cấp runbook giám sát log theo thời gian thực:
    - Backend real-time log: `aws logs tail /ecs/gym-staging-backend --follow`
    - MySQL real-time log: `aws logs tail /ecs/gym-staging-mysql --follow`
