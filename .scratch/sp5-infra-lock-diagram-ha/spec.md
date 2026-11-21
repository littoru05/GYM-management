# [SP5 - DevOps] Khóa Hạ tầng Cloud, Xuất Sơ đồ Kiến trúc AWS & Đảm bảo Tính Sẵn sàng 24/7

Status: ready-for-agent

## Problem Statement

Sau khi hoàn thành chuỗi tính năng triển khai hạ tầng và quy trình phân phối liên tục (từ container hóa ECS Fargate, tích hợp cơ sở dữ liệu bền vững RDS MySQL Single-AZ, đến cơ chế phiên bản hóa Artifact Version và tự động phục hồi Auto-Rollback), dự án cần bước vào giai đoạn chốt chặn và bàn giao sản phẩm:
1. **Thiếu tài liệu trực quan và thuyết minh kiến trúc hoàn chỉnh**: Chưa có sơ đồ kiến trúc tổng thể chi tiết thể hiện toàn bộ các dịch vụ AWS đã triển khai (VPC, Subnets, ALB, ECS Fargate, RDS, ECR, DNS, CloudWatch) cùng quy trình CI/CD chuẩn hóa để đưa vào báo cáo kỹ thuật và trình chiếu trong buổi bảo vệ đồ án.
2. **Nguy cơ rò rỉ thông tin nhạy cảm trong mã nguồn IaC & Thiếu tài liệu đóng gói**: Thư mục mã nguồn hạ tầng Terraform (`infra/`) cần được kiểm toán bảo mật nghiêm ngặt để đảm bảo tuyệt đối không chứa hardcoded secrets hoặc thông tin nhạy cảm, đồng thời cần một bộ hướng dẫn vận hành chuẩn hóa (`README.md`) mô tả chi tiết các bước `init`, `plan`, `apply`, `destroy` cho cả hai môi trường Staging và Production.
3. **Thách thức duy trì tính sẵn sàng 24/7 và ứng cứu sự cố thời gian thực**: Cần đảm bảo hệ thống Production luôn sẵn sàng hoạt động liên tục với đường link live truy cập ổn định, sở hữu bộ công cụ kiểm tra sức khỏe đa tầng (Target Group ALB, ECS Container, Deep Actuator Health, Frontend SPA) và sẵn sàng kịch bản khởi động nhanh khẩn cấp (< 60 giây) nếu xảy ra sự cố mạng hoặc lỗi hệ thống ngay trong buổi bảo vệ.

## Solution

Triển khai gói giải pháp toàn diện cho Sprint 5 (SP5 - DevOps) bao gồm 3 phân hệ chính:

1. **[SUB-1] Bản đồ Kiến trúc AWS Toàn diện & Báo cáo Thuyết minh Kỹ thuật**:
   - Xây dựng sơ đồ kiến trúc hạ tầng AWS Production hoàn chỉnh thể hiện phân vùng mạng (VPC 10.1.0.0/16, Public Subnets, Isolated Private Subnets, Internet Gateway), cân bằng tải (ALB), điều phối container (ECS Fargate Cluster & Service), cơ sở dữ liệu quan hệ (RDS MySQL Single-AZ Free Tier), định danh miền (DuckDNS/Route 53), quản lý container image (ECR), và giám sát tập trung (CloudWatch Logs & Metrics, AWS Budgets).
   - Thiết kế sơ đồ luồng quy trình CI/CD tự động (GitHub Actions OIDC, Multi-stage Docker Build, Pre-Migration Backup, Flyway Expand-Contract, Health Probe Verification, Auto-Rollback).
   - Xuất bản tài liệu thuyết minh kỹ thuật chi tiết (`docs/reports/sp5-aws-architecture-and-cicd-report.md`) và trang trực quan độc lập (HTML/SVG) phục vụ trích xuất ảnh chất lượng cao vào slide và báo cáo.

2. **[SUB-2] Đóng gói, Làm sạch & Kiểm toán Bảo mật Mã nguồn Hạ tầng IaC**:
   - Rà soát tự động toàn bộ mã nguồn Terraform (`infra/`) đảm bảo 0 hardcoded keys (AWS Access Keys, DB Passwords, Secret Tokens), xác thực việc quản lý bí mật qua SSM Parameter Store và biến môi trường nhạy cảm (`sensitive = true`).
   - Chuẩn hóa cấu hình `.gitignore` loại trừ triệt để `.terraform/`, `*.tfstate`, `*.tfvars`.
   - Biên soạn tài liệu hướng dẫn vận hành toàn diện `infra/README.md` (hướng dẫn chi tiết Remote State S3/DynamoDB, bootstrap, lệnh `init`, `plan`, `apply`, `destroy` cho cả Staging và Prod), đồng thời cung cấp file điều hướng tương thích `terraform/README.md`.
   - Xây dựng bộ test tự động xác thực tính bảo mật và sự đầy đủ của tài liệu.

3. **[SUB-3] Trực Vận hành, Giám sát Sức khỏe Đa lớp & Kịch bản Khởi động Nhanh Khẩn cấp**:
   - Cung cấp bộ script giám sát sức khỏe tự động 3 lớp (`scripts/check-ha-health.ps1` và `.sh`): Lớp 1 (AWS ALB Target Health & ECS Task Running State), Lớp 2 (Deep Health Probe `/actuator/health` kiểm tra MySQL & Disk), Lớp 3 (Frontend Nginx SPA HTTP 200 & `/version.json`).
   - Cung cấp bộ script cứu hộ nhanh khẩn cấp (`scripts/quick-restart-prod.ps1` và `.sh`): Hỗ trợ force restart container task < 60s, tự động kích hoạt lại RDS nếu bị stop, và đồng bộ lại Dynamic DNS DuckDNS nếu IP thay đổi.
   - Biên soạn Sổ tay Vận hành Trực chiến Buổi bảo vệ (`docs/runbooks/defense-day-ha-playbook.md`) gồm danh sách URL live, bảng kiểm tra trước giờ bảo vệ (Pre-flight Checklist), ma trận ứng cứu sự cố và các câu lệnh one-liner khẩn cấp.

## User Stories

1. As a project evaluator / capstone reviewer, I want a detailed AWS infrastructure architecture diagram and CI/CD workflow diagram embedded in the technical report, so that I can clearly understand the end-to-end cloud topology, network partitioning, and automated release pipeline.
2. As a DevOps engineer, I want the architecture diagrams exported in high-resolution visual formats (Markdown Mermaid and standalone SVG/HTML), so that they can be directly embedded into slides and written project reports without visual distortion.
3. As a security engineer, I want all Terraform source code in `infra/` to be audited against hardcoded credentials and ensure all secrets are retrieved securely via AWS SSM Parameter Store or sensitive environment variables, so that no credentials are leaked in version control.
4. As an infrastructure administrator, I want comprehensive documentation in `infra/README.md` and a routing reference in `terraform/README.md` detailing backend initialization, variable configuration, and step-by-step commands (`init`, `plan`, `apply`, `destroy`), so that the entire cloud infrastructure can be reproduced reliably by any team member.
5. As a live demo presenter, I want a unified multi-tier health check script that verifies ALB Target Group health, ECS container state, deep Spring Boot Actuator database status, and Frontend HTTP response, so that I can instantly verify system health before stepping onto the stage.
6. As a live demo presenter, I want an emergency quick restart script that can force-refresh ECS tasks and resume RDS connectivity within 60 seconds, so that I can recover immediately if unexpected container or network glitches occur during the defense.
7. As a project team member, I want a concise Defense Day Playbook summarizing live URLs, pre-flight checklists, and one-liner recovery commands, so that anyone on the team can operate and troubleshoot the system under pressure.

## Implementation Decisions

- **Architecture Representation**: Sơ đồ kiến trúc thể hiện trung thực mô hình Production đã triển khai trên AWS Region `ap-southeast-1` (Singapore), bao gồm VPC `10.1.0.0/16`, 2 Public Subnets (cho ALB và ECS Fargate), 2 Private Isolated Subnets (cho RDS MySQL `db.t4g.micro`), Dynamic DNS qua DuckDNS, ECR, CloudWatch, và GitHub Actions OIDC.
- **Directory Structure & Compatibility**: Giữ nguyên thư mục gốc `infra/` để bảo đảm tính tương thích tuyệt đối với các workflow CI/CD (`cd-prod.yml`, `infra-control.yml`) và bộ unit test hiện hành. Tạo file `infra/README.md` đầy đủ và tạo file `terraform/README.md` đóng vai trò điều hướng tương thích.
- **Multi-Tier Health Monitoring**: Script kiểm tra sức khỏe hoạt động độc lập không phụ thuộc môi trường nặng, hỗ trợ cả Windows PowerShell và Linux Bash, có thể kiểm tra qua Internet hoặc qua AWS CLI khi có credentials.
- **Emergency Restart Strategy**: Sử dụng cơ chế rolling task replacement (`--force-new-deployment`) giúp ECS Fargate tạo task mới và chỉ dừng task cũ sau khi task mới vượt qua health check của Target Group, bảo đảm không gây downtime trong quá trình khởi động lại.

## Testing Decisions

- **Seam 1 — Security & IaC Cleanliness Test Suite (`infra/tests/test_terraform_security_and_docs.py`)**: Kiểm thử đệ quy toàn bộ các file `.tf`, `.tfvars`, `.sh`, `.ps1` đảm bảo không chứa hardcoded secrets; kiểm tra cấu trúc `.gitignore`; xác thực sự tồn tại và nội dung các đề mục quan trọng trong `infra/README.md` và `terraform/README.md`.
- **Seam 2 — HA Script & Playbook Verification Suite (`infra/tests/test_ha_scripts_and_playbook.py`)**: Kiểm thử cấu trúc script `check-ha-health`, `quick-restart-prod`, và tính chuẩn xác của các liên kết, URL, câu lệnh cứu hộ trong `defense-day-ha-playbook.md`.
- **Seam 3 — Technical Report & Diagram Validation**: Kiểm thử cú pháp Mermaid và tính nhất quán giữa các tài nguyên được mô tả trong báo cáo kỹ thuật với định nghĩa tài nguyên thực tế trong mã Terraform `infra/environments/prod`.
