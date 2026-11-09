# 01: AWS Infrastructure Architecture Diagram & Technical Report

**What to build:** Thiết kế sơ đồ kiến trúc chi tiết toàn bộ các thành phần hạ tầng AWS đã triển khai cho môi trường Production (VPC, Public Subnets cho ALB & ECS Fargate, Private Isolated Subnets cho RDS MySQL Single-AZ, Internet Gateway, ECR, Route 53/DuckDNS Dynamic DNS, CloudWatch Logs & Metrics, AWS Budgets) và sơ đồ luồng quy trình CI/CD tự động (GitHub Actions OIDC, Multi-stage Docker, Pre-Migration Backup, Flyway Expand-Contract, ECS Rolling Update, Deep Health Probe, Auto-Rollback). Biên soạn tài liệu thuyết minh kiến trúc và quy trình triển khai (`docs/reports/sp5-aws-architecture-and-cicd-report.md`) kèm theo bản trực quan độc lập (HTML/SVG) phục vụ trích xuất ảnh chất lượng cao vào slide và báo cáo đồ án.

Blocked by: none

Status: ready-for-review

- [x] Thiết kế sơ đồ kiến trúc hạ tầng AWS Production bằng cú pháp Mermaid chuẩn, mô tả đầy đủ các tầng mạng (VPC 10.1.0.0/16, Public & Private Subnets, Internet Gateway, Security Groups), tầng cân bằng tải (ALB), tầng tính toán (ECS Fargate Task Backend & Frontend), tầng dữ liệu bền vững (RDS MySQL Single-AZ), định danh (DuckDNS/Route 53), bảo mật (IAM OIDC, SSM Parameter Store), và giám sát (CloudWatch, Budgets).
- [x] Thiết kế sơ đồ luồng quy trình CI/CD hoàn chỉnh bằng Mermaid từ thời điểm lập trình viên commit code/tạo Pull Request đến lúc hệ thống deploy lên ECS Fargate và tự động kích hoạt Auto-Rollback khi gặp sự cố.
- [x] Soạn thảo tài liệu thuyết minh kỹ thuật chi tiết tại `docs/reports/sp5-aws-architecture-and-cicd-report.md` phân tích chuyên sâu về: Nguyên lý thiết kế hệ thống, Phân tích luồng dữ liệu & Điều phối traffic, Cơ chế an toàn cơ sở dữ liệu (Pre-Migration Backup & Flyway Expand-Contract), Chính sách tối ưu hóa chi phí (100% Free Tier Compliance) và Cơ chế tự phục hồi (Self-Healing).
- [x] Tạo file giao diện trực quan độc lập `docs/architecture/sp5-aws-architecture-diagram.html` (hoặc SVG vector) với giao diện hiện đại, hỗ trợ phóng to/thu nhỏ và sao chép/xuất ảnh sắc nét để đưa vào báo cáo và slide thuyết trình.
- [x] Viết test tự động xác thực tính toàn vẹn cú pháp Mermaid và sự hiện diện của các thành phần kiến trúc cốt lõi trong tài liệu báo cáo.
