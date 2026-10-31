# 01: Tinh chỉnh Task Definition ECS Fargate & Đặt trần Chi phí AWS

**Blocked by:** None (can start immediately)

**Type:** task
**Status:** resolved

## Context
Nhằm tối ưu chi phí hạ tầng AWS Fargate Spot cho môi trường Staging và ngăn chặn rủi ro vượt ngưỡng ngân sách (billing spikes), cần:
1. Rà soát và tinh chỉnh cấu hình Task Definition Backend (Spring Boot 21 + MySQL 8.4 sidecar).
2. Thiết lập AWS Budget trần $10/tháng kích hoạt email cảnh báo theo 3 mốc (50% thực tế, 80% thực tế, 100% dự báo).
3. Đọc email nhận cảnh báo từ AWS SSM Parameter Store để tránh lộ thông tin nhạy cảm.

## Requirements
- **ECS Task Definition Backend**:
  - `backend_cpu`: 512 units (0.5 vCPU).
  - `backend_memory`: 2048 MiB.
  - Phân bổ `memoryReservation`: 768 MiB cho container `mysql`, 1024 MiB cho container `backend`.
- **AWS Cost Monitoring Module (`infra/modules/cost-monitoring`)**:
  - Tạo SSM Parameter `/gym/${var.environment}/billing_alert_emails` kiểu `String` với `lifecycle { ignore_changes = [value] }`.
  - Tạo AWS Budget (`aws_budgets_budget`) loại `COST`, hạn mức `10` USD, chu kỳ `MONTHLY`.
  - Cấu hình 3 notification:
    - 50% `ACTUAL` (> $5.00)
    - 80% `ACTUAL` (> $8.00)
    - 100% `FORECASTED` (> $10.00)
- **Staging Environment Integration**:
  - Tích hợp module `cost-monitoring` vào `infra/environments/staging/main.tf` và `outputs.tf`.
- **Kiểm thử tự động**:
  - Viết `infra/tests/test_cost_monitoring_and_task_def.py` kiểm tra cấu hình HCL, sizing, limits, và tích hợp.

## Answer
- **ECS Task Definition Backend**:
  - Đã cập nhật `infra/modules/ecs/variables.tf`: `backend_cpu` giảm xuống `512` units (0.5 vCPU, tiết kiệm 50% chi phí compute), giữ `backend_memory` ở `2048` MiB để đảm bảo an toàn tuyệt đối tránh OOM.
  - Đã cập nhật `infra/modules/ecs/main.tf`: Bổ sung `memoryReservation = 768` cho container `mysql:8.4` và `memoryReservation = 1024` cho container `backend` (Spring Boot 21), giúp phân bổ tài nguyên tối thiểu rõ ràng mà vẫn cho phép dùng chung RAM dôi dư.
- **Module Cost Monitoring (`infra/modules/cost-monitoring`)**:
  - Tạo `aws_ssm_parameter.billing_alert_emails` (`/gym/${var.environment}/billing_alert_emails`) với `lifecycle { ignore_changes = [value] }` để người dùng có thể cập nhật email thật qua AWS Console / CLI mà không bị ghi đè.
  - Tạo `aws_budgets_budget.monthly_cost` với ngân sách $10/tháng và 3 trigger cảnh báo email: 50% Actual ($5), 80% Actual ($8), và 100% Forecasted ($10).
  - Tích hợp module vào `infra/environments/staging/main.tf` và xuất outputs tại `infra/environments/staging/outputs.tf`.
- **Kiểm thử & Định nghĩa Hoàn thành (DoD)**:
  - Viết bộ kiểm thử `infra/tests/test_cost_monitoring_and_task_def.py` kiểm tra cấu trúc HCL, thông số sizing, soft limits, và cấu hình Budget/SSM.
  - Chạy `terraform fmt` và `terraform validate` thành công 100% không có lỗi.
  - Toàn bộ 32 unit tests của infra đều vượt qua.
