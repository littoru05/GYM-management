# 03: HA Monitoring, Multi-Tier Health Checks & Emergency Quick Restart Runbook

**What to build:** Xây dựng giải pháp đảm bảo tính sẵn sàng cao (High Availability) và phương án ứng cứu khẩn cấp phục vụ buổi bảo vệ đồ án: Cung cấp bộ script kiểm tra sức khỏe đa tầng độc lập (`scripts/check-ha-health.ps1` và `.sh`) kiểm tra đồng thời ALB Target Group health, ECS Container running status, Spring Boot Actuator deep health (`/actuator/health` với MySQL & disk space), và Frontend Nginx SPA HTTP 200; Cung cấp script khởi động lại nhanh khẩn cấp (`scripts/quick-restart-prod.ps1` và `.sh`) thực hiện force update ECS service không downtime và tự động đánh thức RDS nếu bị tạm dừng; Biên soạn Sổ tay Vận hành Trực chiến (`docs/runbooks/defense-day-ha-playbook.md`) chứa danh sách URL live, checklist trước giờ bảo vệ và các lệnh one-liner cứu hộ nhanh; Viết test tự động kiểm thử toàn bộ script và playbook.

Blocked by: none

Status: ready-for-agent

- [x] Phát triển script kiểm tra sức khỏe đa tầng `scripts/check-ha-health.ps1` (PowerShell cho Windows) và `scripts/check-ha-health.sh` (Bash cho Linux/macOS) kiểm tra 3 tầng:
  - Tầng 1 (Hạ tầng AWS): ALB Target Group Health (báo cáo số lượng healthy/unhealthy targets) và ECS Service/Task state.
  - Tầng 2 (Deep Backend Health): Endpoint `/actuator/health` (xác thực HTTP 200, status "UP", kết nối MySQL và dung lượng ổ đĩa).
  - Tầng 3 (Frontend Web): URL gốc `/` và `/version.json` (xác thực HTTP 200 và hiển thị Artifact Version đang chạy).
  - Hỗ trợ chế độ kiểm tra nhanh chỉ qua HTTP (không cần AWS credentials) và chế độ kiểm tra đầy đủ (kết hợp AWS CLI).
- [x] Phát triển script cứu hộ nhanh khẩn cấp `scripts/quick-restart-prod.ps1` và `scripts/quick-restart-prod.sh` với khả năng:
  - Tự động kiểm tra trạng thái RDS Production (`aws rds describe-db-instances`), nếu đang stopped thì kích hoạt start và chờ trạng thái available.
  - Kích hoạt rolling task update trên ECS Fargate (`aws ecs update-service --force-new-deployment`) cho Backend và Frontend.
  - Kiểm tra và tự động tái đồng bộ Dynamic DNS DuckDNS nếu ALB public IP thay đổi.
  - Đảm bảo thời gian phục hồi dịch vụ hoàn tất trong vòng 60 giây.
- [x] Biên soạn tài liệu Sổ tay Vận hành Trực chiến Buổi bảo vệ (`docs/runbooks/defense-day-ha-playbook.md`) gồm:
  - Bảng tổng hợp Live URL Production (DuckDNS Domain, ALB Raw CNAME, Actuator Health, Swagger Docs).
  - Pre-flight Checklist 15 phút trước khi thuyết trình.
  - Ma trận xử lý nhanh các tình huống sự cố (DNS failover, Task OOM/Crash, RDS connectivity loss, Network latency).
  - Danh mục lệnh One-Liner khẩn cấp để thao tác ngay trên terminal chỉ với 1 cú click/paste.
- [x] Xây dựng test suite tự động `infra/tests/test_ha_scripts_and_playbook.py` kiểm thử tính đúng đắn của tham số script, cú pháp câu lệnh và tính hợp lệ của Playbook trực chiến.
