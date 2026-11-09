# Điều hướng Mã Nguồn Hạ Tầng Terraform (IaC Navigation)

> **Thông báo quan trọng về Cấu trúc Thư mục & Tính Tương thích CI/CD:**
> Toàn bộ mã nguồn Terraform IaC (Infrastructure as Code) chính thức của dự án GYM Management được tổ chức và quản lý tập trung tại thư mục:
>
> 👉 **[`../infra/`](../infra/)** (Tài liệu chi tiết: **[`../infra/README.md`](../infra/README.md)**)

---

## 1. Lý do Duy trì Thư mục `infra/`

Để đảm bảo tính nhất quán và tương thích ngược 100% với:
1. **GitHub Actions Workflows (CI/CD)**:
   - `.github/workflows/cd-staging.yml`
   - `.github/workflows/cd-prod.yml`
   - `.github/workflows/infra-control.yml`
   - `.github/workflows/ci.yml`
2. **Bộ Test Suite Tự Động (Infrastructure Validation)**:
   - Toàn bộ 13+ module kiểm thử tự động tại `infra/tests/` (bao gồm kiểm toán an ninh, cấu trúc module networking, ALB, RDS, ECS, OIDC, v.v.).
3. **Các Script Vận hành & Cứu hộ Khẩn cấp**:
   - `infra/scripts/bootstrap-state.sh` & `bootstrap-state.ps1`
   - `scripts/check-ha-health.sh` & `scripts/quick-restart-prod.sh`

Do đó, thư mục `terraform/` này được duy trì làm **cầu nối điều hướng tương thích** giúp người chấm điểm, kiểm toán viên và nhà phát triển dễ dàng định vị tài nguyên theo đúng quy chuẩn đề bài mà không làm gián đoạn hạ tầng đang vận hành thực tế.

---

## 2. Bảng Ánh Xạ Đường Dẫn (Directory Mapping)

| Đường dẫn Quy chiếu (`terraform/`) | Đường dẫn Thực tế Dự án (`infra/`) | Mô tả Chức năng |
| :--- | :--- | :--- |
| `terraform/bootstrap/` | [`infra/bootstrap/`](../infra/bootstrap/) | Khởi tạo S3 Remote State Bucket & DynamoDB State Lock Table |
| `terraform/environments/staging/` | [`infra/environments/staging/`](../infra/environments/staging/) | Hạ tầng môi trường Staging (ALB + ECS Fargate + MySQL Sidecar) |
| `terraform/environments/prod/` | [`infra/environments/prod/`](../infra/environments/prod/) | Hạ tầng môi trường Production (VPC Isolated, ALB, ECS Fargate, RDS MySQL Single-AZ) |
| `terraform/modules/networking/` | [`infra/modules/networking/`](../infra/modules/networking/) | VPC, Public/Private Subnets, IGW, Route Tables, Security Groups |
| `terraform/modules/alb/` | [`infra/modules/alb/`](../infra/modules/alb/) | Application Load Balancer, Listener Port 80, Target Groups, Health Checks |
| `terraform/modules/rds/` | [`infra/modules/rds/`](../infra/modules/rds/) | AWS RDS MySQL Single-AZ (Free Tier `db.t4g.micro`, gp2, Private Subnets) |
| `terraform/modules/ecs/` | [`infra/modules/ecs/`](../infra/modules/ecs/) | ECS Fargate Cluster, Task Definitions (Backend + Frontend), SSM Parameters |
| `terraform/modules/ecr/` | [`infra/modules/ecr/`](../infra/modules/ecr/) | Singleton ECR Repositories (`gym-backend`, `gym-frontend`) & Lifecycle Policies |
| `terraform/modules/ci-oidc/` | [`infra/modules/ci-oidc/`](../infra/modules/ci-oidc/) | GitHub Actions OIDC Authentication & IAM Roles (No hardcoded keys) |
| `terraform/modules/cost-monitoring/` | [`infra/modules/cost-monitoring/`](../infra/modules/cost-monitoring/) | AWS Budgets Alert ($10 USD ceiling) |
| `terraform/tests/` | [`infra/tests/`](../infra/tests/) | Bộ kiểm thử tự động Python unittest cho Terraform |

---

## 3. Bắt đầu Nhanh (Quick Start)

Vui lòng tham khảo tài liệu hướng dẫn vận hành toàn diện tại:
📖 **[`infra/README.md`](../infra/README.md)**

Các câu lệnh cơ bản:
```bash
# Di chuyển vào môi trường mong muốn
cd infra/environments/staging   # Hoặc: cd infra/environments/prod

# Khởi tạo Terraform và tải providers
terraform init

# Lập kế hoạch thay đổi hạ tầng
terraform plan

# Áp dụng hạ tầng
terraform apply
```
