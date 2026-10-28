# Spec: [SP4 - DevOps] Tối ưu Tài nguyên Fargate, Giám sát CloudWatch & Kiểm soát Chi phí AWS

## Overview
Giai đoạn SP4 tập trung vào việc rà soát tối ưu hóa chi phí vận hành dịch vụ trên AWS (Fargate compute), thiết lập hàng rào kiểm soát ngân sách tự động (AWS Budgets), và xây dựng dashboard giám sát CloudWatch.

## Subtasks
1. **01-task-def-tuning-and-billing-alarm**: Tinh chỉnh Task Definition ECS Fargate (giảm vCPU xuống 512 units, tối ưu phân bổ RAM qua soft limits 768 MiB MySQL và 1024 MiB Backend) & Thiết lập AWS Budget (trần $10/tháng, cảnh báo 3 mốc: 50% thực tế, 80% thực tế, 100% dự báo) đọc email an toàn qua SSM Parameter Store.
2. **02-cloudwatch-centralized-logging**: Cấu hình Quản lý Log Tập trung qua AWS CloudWatch Logs (awslogs driver cho Spring Boot & MySQL sidecar, retention policy 7 ngày tối ưu chi phí, loại bỏ ANSI color).
3. **03-cloudwatch-monitoring-dashboard**: Thiết lập CloudWatch Dashboard và Metric Alarms giám sát hiệu năng ECS Fargate (CPU, RAM utilization) và ALB (Response time, 5XX errors).

## Definition of Done (DoD) - Subtask 01
- Task Definition Backend ECS Fargate được tinh chỉnh về mức tối ưu (512 vCPU units, 2048 MiB RAM, soft memory reservations: MySQL 768 MiB, Spring Boot 1024 MiB thông qua biến cấu hình).
- Module `cost-monitoring` quản lý AWS Budget $10/tháng và SSM Parameter Store `/gym/staging/billing_alert_emails` hoạt động chuẩn xác, tích hợp vào môi trường Staging.
- Toàn bộ unit tests Python kiểm thử hạ tầng và Terraform validate đều pass 100%.

## Definition of Done (DoD) - Subtask 02
- Toàn bộ log console của ứng dụng (Spring Boot và MySQL) hiển thị trực tiếp trên CloudWatch Log Streams theo thời gian thực qua awslogs driver.
- Chính sách lưu trữ (Retention Policy) 7 ngày được áp dụng đồng bộ cho các log groups qua biến `log_retention_in_days`.
- Loại bỏ mã màu ANSI (`SPRING_OUTPUT_ANSI_ENABLED=NEVER`) cho container backend.
- Xuất Terraform outputs cho tên và ARN của các CloudWatch Log Groups.
- Bộ unit test `test_cloudwatch_logging.py` và Terraform validate pass 100%.
