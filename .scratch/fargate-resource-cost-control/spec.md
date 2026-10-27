# Spec: [SP4 - DevOps] Tối ưu Tài nguyên Fargate, Giám sát CloudWatch & Kiểm soát Chi phí AWS

## Overview
Giai đoạn SP4 tập trung vào việc rà soát tối ưu hóa chi phí vận hành dịch vụ trên AWS (Fargate compute), thiết lập hàng rào kiểm soát ngân sách tự động (AWS Budgets), và xây dựng dashboard giám sát CloudWatch.

## Subtasks
1. **01-task-def-tuning-and-billing-alarm**: Tinh chỉnh Task Definition ECS Fargate (giảm vCPU/RAM tối ưu chi phí, phân bổ soft limits) & Thiết lập AWS Billing Alarm / AWS Budget (trần $10, cảnh báo 3 mốc: 50% thực tế, 80% thực tế, 100% dự báo) đọc email an toàn qua SSM Parameter Store.

## Definition of Done (DoD)
- Task Definition Backend ECS Fargate được tinh chỉnh về mức tối ưu (512 vCPU units, 2048 MiB RAM, soft limit: MySQL 768 MiB, Spring Boot 1024 MiB).
- Module `cost-monitoring` quản lý AWS Budget $10/tháng và SSM Parameter Store `/gym/staging/billing_alert_emails` hoạt động chuẩn xác, tích hợp vào môi trường Staging.
- Toàn bộ unit tests Python kiểm thử hạ tầng và Terraform validate đều pass 100%.
