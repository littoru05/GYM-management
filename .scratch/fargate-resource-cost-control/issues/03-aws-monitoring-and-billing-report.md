# 03: Xuất Báo cáo Giám sát Tài nguyên & Vận hành Hạ tầng AWS

**Blocked by:** 01-task-def-tuning-and-billing-alarm.md, 02-cloudwatch-centralized-logging.md

**Type:** task
**Status:** resolved

## Context
Thu thập biểu đồ CPU Utilization, Memory Utilization từ CloudWatch Metrics trong thời gian Tester thực thi kịch bản hồi quy; thống kê hóa đơn thực tế trong AWS Billing Dashboard.

## Requirements
- File báo cáo `Bao_cao_giam_sat_tai_nguyen_AWS.pdf` hoàn thiện lưu vào thư mục `docs/`.
- Thu thập số liệu thực tế từ AWS CLI / CloudWatch Metrics (`CPUUtilization`, `MemoryUtilization`, `LiveTaskCount`, `TargetResponseTime`) cho các service `gym-staging-backend` và `gym-staging-frontend`.
- Trực quan hóa cả 2 cấp độ biểu đồ: Tổng quan toàn chiến dịch và Zoom chi tiết từng phút trong phiên kiểm thử hồi quy.
- Thống kê chi tiết hóa đơn thực tế tháng 10/2026 từ AWS Cost Explorer (Gross Usage Cost, Credits, Net Cost) và kiểm soát ngân sách AWS Budgets ($10/tháng).
- Phân tích hiệu năng, an toàn bộ nhớ (không OOM), và hiệu quả kinh tế của kiến trúc Fargate Spot + Pause/Hibernate.
- Cung cấp script tự động hóa `scripts/generate-aws-monitoring-report.py` để tái sinh báo cáo khi có chu kỳ kiểm thử mới.

## Definition of Done (DoD)
- File `docs/Bao_cao_giam_sat_tai_nguyen_AWS.pdf` hoàn thiện, hiển thị chuẩn 3 trang A4 sắc nét, hỗ trợ tiếng Việt Unicode chuẩn.
- Script trích xuất và sinh báo cáo hoạt động tự động 100%.
- Kiểm thử tự động `infra/tests/test_monitoring_report.py` và toàn bộ test suite infra pass 100%.

## Answer
- **Số liệu Thực tế Thu thập từ AWS**:
  - **AWS CloudWatch Metrics (`AWS/ECS`)**:
    - Backend CPU trung bình cả tháng: **1.72%**, đỉnh boot container: **123.57%**, khi thực thi kịch bản hồi quy: **1.15% – 1.94%** (đỉnh nhỏ 5.17%).
    - Backend RAM trung bình: **44.96%** (~921 MiB), đỉnh: **50.44%** (~1033 MiB), hoàn toàn không rò rỉ bộ nhớ, dư hơn 50% vùng an toàn trên Task Definition 2048 MiB.
    - Frontend CPU trung bình: **0.017%**, RAM: **0.58%** (~3 MiB).
    - ALB Target Response Time trung bình: **0.101s – 0.218s**.
  - **AWS Billing & Cost Explorer (Tháng 10/2026)**:
    - Tổng chi phí tiêu thụ gốc (Gross Usage Cost): **$5.916 USD** (ALB: $3.705 - 62.6%, VPC Public IPv4: $1.330 - 22.5%, ECS Fargate Spot: $0.833 - 14.1%, ECR: $0.048 - 0.8%, S3/DynamoDB/Logs: $0.001).
    - AWS Credit giảm trừ: **-$5.916 USD** (khấu trừ 100%).
    - Hóa đơn thực thanh toán (Net Billed Cost): **$0.00 USD**.
    - AWS Budgets: Hạn mức $10.00/tháng, ActualSpend: **$5.916 USD** (kích hoạt cảnh báo sớm 50% = $5), ForecastedSpend: **$5.946 USD** (nằm an toàn dưới mốc 80% = $8 và trần $10.00).
- **Sản phẩm Đóng gói**:
  - File PDF hoàn thiện: `docs/Bao_cao_giam_sat_tai_nguyen_AWS.pdf` (107 KB, 3 trang A4 chuẩn format, tích hợp biểu đồ Vector trực quan, bảng số liệu chi tiết).
  - Script tự động: `scripts/generate-aws-monitoring-report.py` sử dụng thư viện ReportLab + phông chữ Arial Unicode.
  - Bộ unit test `infra/tests/test_monitoring_report.py` kiểm chứng file PDF và script sinh báo cáo, tích hợp vào test suite của dự án (42/42 tests pass).
