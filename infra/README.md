# Hướng Dẫn Vận Hành Hạ Tầng IaC (Infrastructure as Code) - Terraform

Tài liệu này cung cấp hướng dẫn toàn diện về kiến trúc, cấu hình an ninh, quy trình khởi tạo, triển khai và tiêu hủy hạ tầng đám mây AWS của dự án **GYM Management** bằng công cụ Terraform.

---

## 1. Tổng Quan Kiến Trúc IaC & Sơ Đồ Thư Mục

Hạ tầng được thiết kế theo nguyên tắc **Modular Architecture** (kiến trúc module hóa cao), tuân thủ tiêu chuẩn an ninh đám mây và tối ưu hóa chi phí trong hạn mức **AWS Free Tier**:

- **Tách biệt môi trường (Environment Isolation)**: Staging và Production sở hữu VPC độc lập, CIDR riêng biệt và cấu hình cơ sở dữ liệu riêng (Staging dùng container sidecar tiết kiệm, Production dùng RDS MySQL Single-AZ cô lập).
- **Tái sử dụng module (DRY Infrastructure)**: Toàn bộ thành phần hạ tầng (VPC Networking, ALB, ECS Fargate, RDS, ECR, CI-OIDC, Cost Monitoring) được đóng gói thành các module độc lập tại `infra/modules/*`.
- **Quản trị Remote State tập trung**: State được lưu trữ bền vững tại Amazon S3 với cơ chế mã hóa AES-256, bật phiên bản hóa (Versioning) và khóa trạng thái đồng thời (State Locking) bằng Amazon DynamoDB.

### Sơ đồ Cây Thư Mục Module & Chi Tiết Các Module

```text
infra/
├── .gitignore                          # Loại trừ tfstate, tfvars, .terraform, lock files
├── README.md                           # Tài liệu hướng dẫn vận hành hạ tầng toàn diện
├── bootstrap/                          # Khởi tạo S3 Remote State & DynamoDB Lock Table
│   ├── .terraform.lock.hcl             # Dependency lock file cho providers
│   ├── main.tf                         # Khai báo S3 bucket & DynamoDB table
│   └── outputs.tf                      # Tên S3 bucket và DynamoDB lock table
├── environments/
│   ├── staging/                        # Môi trường Staging (Development / Testing)
│   │   ├── backend.tf                  # Cấu hình S3 remote backend (key: staging/terraform.tfstate)
│   │   ├── main.tf                     # Kết nối VPC (10.0.0.0/16), ALB, ECS (sidecar MySQL), Budget
│   │   ├── variables.tf                # Khai báo biến đầu vào cho Staging
│   │   └── outputs.tf                  # DNS ALB, ECR URL, ECS cluster/services
│   └── prod/                           # Môi trường Production (Sẵn sàng 24/7)
│       ├── backend.tf                  # Cấu hình S3 remote backend (key: prod/terraform.tfstate)
│       ├── main.tf                     # Kết nối VPC (10.1.0.0/16), ALB, ECS Fargate, RDS MySQL
│       ├── variables.tf                # Khai báo biến nhạy cảm (sensitive = true)
│       └── outputs.tf                  # DNS ALB, RDS endpoint, CloudWatch logs
├── modules/
│   ├── networking/                     # VPC, Public/Private Subnets, IGW, Security Groups
│   ├── alb/                            # Application Load Balancer, Listener Port 80, Target Groups
│   ├── rds/                            # Single-AZ RDS MySQL (db.t4g.micro Free Tier, gp2 20GB)
│   ├── ecs/                            # ECS Fargate Cluster, Task Definitions, SSM Parameter Store
│   ├── ecr/                            # ECR Repositories (gym-backend, gym-frontend), Lifecycle Policy
│   ├── ci-oidc/                        # GitHub Actions OIDC Role & IAM Policies
│   └── cost-monitoring/                # AWS Budgets Alert ($10 ceiling) & SNS topic
├── scripts/
│   ├── bootstrap-state.sh              # Script Bash khởi tạo remote state tự động
│   └── bootstrap-state.ps1             # Script PowerShell khởi tạo remote state tự động
└── tests/                              # Bộ kiểm thử tự động xác thực mã nguồn IaC & an ninh
```

| Tên Module | Thư mục | Chức năng chính |
| :--- | :--- | :--- |
| **Networking** | `modules/networking` | Quản lý VPC, Public Subnets, Isolated Database Subnets, IGW, Security Groups |
| **ALB** | `modules/alb` | Application Load Balancer, Listener Port 80, Target Groups, Health Checks |
| **RDS** | `modules/rds` | AWS RDS MySQL Single-AZ (Free Tier `db.t4g.micro`, gp2 20GB) |
| **ECS** | `modules/ecs` | ECS Fargate Services, Task Definitions (Backend + Frontend), SSM Parameters |
| **ECR** | `modules/ecr` | Singleton ECR Repositories (`gym-backend`, `gym-frontend`) & Lifecycle Policies |
| **CI-OIDC** | `modules/ci-oidc` | GitHub Actions OIDC Authentication & IAM Roles (Zero hardcoded keys) |
| **Cost Monitoring** | `modules/cost-monitoring` | AWS Budgets Alert ($10 USD ceiling) |

---

## 2. Yêu Cầu Tiên Quyết (Prerequisites)

Trước khi thực thi các lệnh Terraform, quản trị viên cần chuẩn bị đầy đủ môi trường:

### 2.1. Công Cụ Yêu Cầu
1. **Terraform CLI >= 1.5** (hoặc Terraform CLI v1.5+, khuyến nghị phiên bản chuẩn hóa trong CI/CD: `v1.9.5`).
   ```bash
   terraform version
   ```
2. **AWS CLI v2** (hoặc AWS CLI 2.x trở lên).
   ```bash
   aws --version
   ```
3. **Git**: Để quản lý phiên bản mã nguồn.
4. **Python**: Phiên bản `3.9+` (phục vụ chạy test suite xác thực an ninh).

### 2.2. Quyền Hạn IAM (IAM Permissions)
Tài khoản IAM hoặc IAM Role chạy Terraform cần có quyền thực thi trên các dịch vụ AWS sau:
- **Amazon S3**: Tạo bucket, bật versioning, đặt chính sách mã hóa và chặn public access (`s3:*`).
- **Amazon DynamoDB**: Tạo bảng khóa trạng thái với khóa phân vùng `LockID` (`dynamodb:*`).
- **Amazon VPC & EC2**: Quản lý VPC, Subnets, Route Tables, Internet Gateways, Elastic IPs, Security Groups (`ec2:*`).
- **Elastic Load Balancing (ALB)**: Tạo và cấu hình Load Balancer, Target Groups, Listeners, Health Checks (`elasticloadbalancing:*`).
- **Amazon ECS**: Quản lý ECS Cluster, Fargate Services, Task Definitions, CloudWatch Log Groups (`ecs:*`).
- **Amazon RDS**: Quản trị DB Instance, DB Subnet Group, Parameter Groups (`rds:*`).
- **Amazon ECR**: Đọc/ghi kho chứa Docker images và thiết lập lifecycle policy (`ecr:*`).
- **AWS Systems Manager (SSM)**: Đọc/ghi tham số an toàn `SecureString` (`ssm:*`).
- **AWS Budgets**: Tạo cảnh báo ngân sách chi phí (`budgets:*`).
- **AWS IAM**: Tạo OIDC Provider và IAM Roles cho GitHub Actions CI/CD (`iam:*`).

Đăng nhập và kiểm tra thông tin định danh:
```bash
aws configure
aws sts get-caller-identity
```

---

## 3. Hướng Dẫn Thiết Lập Remote State Backend (`infra/bootstrap`)

Remote State Backend đảm bảo toàn bộ nhóm phát triển và hệ thống CI/CD đồng bộ trạng thái hạ tầng an toàn, ngăn chặn xung đột ghi đè song song.

### 3.1. Khởi Tạo Tự Động Bằng Script
Dự án cung cấp sẵn script tự động hóa khởi tạo S3 Bucket và DynamoDB Table:

**Trên Linux / macOS (Bash):**
```bash
chmod +x infra/scripts/bootstrap-state.sh
./infra/scripts/bootstrap-state.sh "ap-southeast-1" "gym-management-tfstate-staging" "gym-management-tflock-staging"
```

**Trên Windows (PowerShell):**
```powershell
.\infra\scripts\bootstrap-state.ps1 -Region "ap-southeast-1" -BucketName "gym-management-tfstate-staging" -TableName "gym-management-tflock-staging"
```

### 3.2. Khởi Tạo Thủ Công Bằng Terraform
Bạn cũng có thể khởi tạo trực tiếp bằng thư mục `infra/bootstrap`:

```bash
cd infra/bootstrap

# 1. Khởi tạo Terraform
terraform init

# 2. Xem trước kế hoạch tạo tài nguyên S3 & DynamoDB
terraform plan -out=bootstrap.tfplan

# 3. Áp dụng triển khai
terraform apply bootstrap.tfplan
```

**Tài nguyên được tạo ra:**
- **S3 Bucket**: `gym-management-tfstate-staging` (Bật Versioning, AES-256 Server-Side Encryption, Chặn 100% Public Access).
- **DynamoDB Table**: `gym-management-tflock-staging` (Billing Mode: `PAY_PER_REQUEST`, Partition Key: `LockID` [String]).

---

## 4. Hướng Dẫn Triển Khai Môi Trường Staging (`infra/environments/staging`)

Môi trường Staging phục vụ thử nghiệm tính năng nhanh, tích hợp liên tục và kiểm thử tích hợp.

### 4.1. Quy Trình Các Lệnh Thực Hiện

```bash
cd infra/environments/staging

# 1. Khởi tạo và kết nối tới S3 Remote Backend
terraform init

# 2. Kiểm tra định dạng và tính hợp lệ cú pháp
terraform fmt -check
terraform validate

# 3. Lập kế hoạch triển khai (Plan)
terraform plan -out=staging.tfplan

# 4. Áp dụng kế hoạch triển khai (Apply)
terraform apply staging.tfplan
```

### 4.2. Các Thông Số Hạ Tầng Staging
- **VPC CIDR**: `10.0.0.0/16`
- **Subnets**: 2 Public Subnets (`10.0.1.0/24`, `10.0.2.0/24`)
- **ALB**: `gym-staging-alb`
- **ECS Cluster**: `gym-staging-cluster`
- **Cơ chế DB**: MySQL 8.4 Sidecar container chạy cùng task backend (tối ưu chi phí $0 compute cho database).
- **Chi phí dự toán**: Nằm trọn vẹn trong AWS Free Tier.

---

## 5. Hướng Dẫn Triển Khai Môi Trường Production (`infra/environments/prod`)

Môi trường Production được thiết kế để đảm bảo **tính sẵn sàng 24/7**, an toàn dữ liệu với cơ sở dữ liệu bền vững RDS MySQL Single-AZ nằm trong Private Subnets biệt lập.

### 5.1. Cấu Hình Biến Nhạy Cảm (Environment Variables)
Trước khi chạy `terraform plan` hoặc `terraform apply`, cấu hình các biến nhạy cảm thông qua biến môi trường để đảm bảo không ghi vết mật khẩu vào terminal history:

```bash
# Trên Linux / macOS / Git Bash
export TF_VAR_db_password="YourStrongProdPassword123!"
export TF_VAR_jwt_secret="YourProductionJWTSecretStringAtLeast32BytesLength"

# Trên Windows PowerShell
$env:TF_VAR_db_password="YourStrongProdPassword123!"
$env:TF_VAR_jwt_secret="YourProductionJWTSecretStringAtLeast32BytesLength"
```

### 5.2. Quy Trình Các Lệnh Thực Hiện

```bash
cd infra/environments/prod

# 1. Khởi tạo và kết nối tới S3 Remote Backend (key: prod/terraform.tfstate)
terraform init

# 2. Kiểm tra cú pháp và tính toàn vẹn cấu trúc
terraform fmt -check
terraform validate

# 3. Tạo kế hoạch triển khai chi tiết
terraform plan -out=prod.tfplan

# 4. Triển khai hạ tầng Production
terraform apply prod.tfplan
```

### 5.3. Các Thông Số Hạ Tầng Production
- **VPC CIDR**: `10.1.0.0/16` (Phân vùng hoàn toàn độc lập với Staging).
- **Public Subnets**: `10.1.1.0/24`, `10.1.2.0/24` (Dành cho ALB và ECS tasks kết nối Internet).
- **Isolated Private Subnets**: `10.1.10.0/24`, `10.1.11.0/24` (Không có Internet Gateway/NAT Gateway, chỉ cho phép cổng 3306 từ Security Group của ECS Tasks).
- **ALB**: `gym-prod-alb` (Port 80 -> Định tuyến `/api/*` tới Backend, `/*` tới Frontend).
- **Database (RDS)**: Single-AZ MySQL 8.4 (`db.t4g.micro`, 20GB gp2 Storage, tự động sao lưu 1 ngày).
- **Container Tasks**: ECS Fargate Backend (`512` CPU, `1024` MiB RAM), Frontend (`256` CPU, `512` MiB RAM).

Sau khi apply thành công, Terraform sẽ xuất các giá trị đầu ra quan trọng:
- `alb_dns_name`: Địa chỉ DNS public của bộ cân bằng tải ALB.
- `rds_endpoint`: Địa chỉ kết nối nội bộ của RDS MySQL.
- `ecs_cluster_name`: Tên cụm ECS Fargate.

---

## 6. Hướng Dẫn Cấu Hình Biến Nhạy Cảm & Quản Lý State An Toàn

Bảo mật là tiêu chí hàng đầu trong quản trị hạ tầng IaC của dự án. Hệ thống triển khai mô hình đa lớp chống rò rỉ secrets:

### 6.1. Nguyên Tắc 0 Hardcoded Secret Trong Git
- **Tuyệt đối không lưu secret trong file code**: Không hardcode AWS Access Keys, DB Passwords, JWT Tokens trực tiếp trong `.tf` hay `.tfvars`.
- **Đánh dấu flag `sensitive = true`**: Mọi biến nhạy cảm (`db_password`, `jwt_secret`, `password`) trong `variables.tf` đều bắt buộc cấu hình thuộc tính `sensitive = true`. Thuộc tính này ngăn chặn Terraform in giá trị mật ra màn hình console hoặc log CI/CD.

### 6.2. Lưu Trữ Mật Khẩu Qua AWS SSM Parameter Store
- Toàn bộ thông tin nhạy cảm của ứng dụng được đẩy vào **AWS Systems Manager Parameter Store** dưới định dạng mã hóa cao cấp `SecureString`:
  - `/gym/${environment}/db_password`
  - `/gym/${environment}/jwt_secret`
- ECS Fargate Task Definition chỉ nạp giá trị mật qua thuộc tính `secrets` (tham chiếu ARN của SSM Parameter Store), hoàn toàn không khai báo giá trị thô trong `environment`.
- Khối tài nguyên SSM Parameter Store được cấu hình:
  ```hcl
  lifecycle {
    ignore_changes = [value]
  }
  ```
  Điều này cho phép quản trị viên cập nhật hoặc đổi mật khẩu trực tiếp trên AWS Console/CLI mà không bị ghi đè khi chạy lại `terraform apply`.

### 6.3. Loại Trừ File Nhạy Cảm Bằng `.gitignore`
Hệ thống cấu hình chặt chẽ cả `infra/.gitignore` và root `.gitignore` để tự động loại bỏ:
- Thư mục thư viện cục bộ: `.terraform/`, `**/.terraform/*`
- Trạng thái hạ tầng cục bộ: `*.tfstate`, `*.tfstate.*`, `*.tfstate.backup`, `*.backup`
- File biến nhạy cảm: `*.tfvars`, `*.tfvars.json`
- File nhật ký sự cố và khóa lock: `crash.log`, `.terraform.tfstate.lock.info`, `*.tfplan`
- File môi trường cục bộ: `.env`, `.env.*`

---

## 7. Hướng Dẫn Tiêu Hủy Tài Nguyên Có Kiểm Soát (`terraform destroy`)

Khi cần gỡ bỏ hạ tầng nhằm tiết kiệm chi phí hoặc tái tạo từ đầu, quản trị viên cần tuân thủ quy trình tiêu hủy có kiểm soát.

> [!WARNING]
> Lệnh `terraform destroy` sẽ xóa vĩnh viễn toàn bộ tài nguyên (ALB, ECS Containers, Database). Hãy đảm bảo dữ liệu quan trọng đã được sao lưu trước khi thực hiện!

### 7.1. Tiêu Hủy Môi Trường Staging
```bash
cd infra/environments/staging

# Xem trước các tài nguyên sẽ bị xóa
terraform plan -destroy

# Thực hiện tiêu hủy có xác nhận
terraform destroy
```

### 7.2. Tiêu Hủy Môi Trường Production
Đối với Production, do RDS MySQL chứa dữ liệu thực tế, khuyến nghị thực hiện các bước:
1. **Kiểm tra và Snapshot Database (nếu cần bảo lưu)**:
   ```bash
   aws rds create-db-snapshot \
     --db-instance-identifier gym-prod-mysql \
     --db-snapshot-identifier gym-prod-mysql-manual-backup-$(date +%Y%m%d) \
     --region ap-southeast-1
   ```
2. **Thực thi tiêu hủy qua Terraform**:
   ```bash
   cd infra/environments/prod
   terraform plan -destroy
   terraform destroy
   ```

### 7.3. Tiêu Hủy Remote State Backend (Chỉ khi hủy hoàn toàn dự án)
Remote State Bucket và DynamoDB Table trong `infra/bootstrap` được cấu hình `force_destroy = false` nhằm chống xóa nhầm. Nếu muốn xóa:
1. Xóa toàn bộ file state trong S3 bucket (bao gồm các version cũ).
2. Di chuyển vào `infra/bootstrap` và thực hiện:
   ```bash
   cd infra/bootstrap
   terraform destroy
   ```

---

## 8. Tự Động Hóa Kiểm Thử & Kiểm Toán Bảo Mật (Automated Testing)

Dự án tích hợp bộ kiểm thử tự động toàn diện kiểm toán an ninh và cấu trúc mã nguồn IaC:

```bash
# Chạy kiểm tra an ninh và tài liệu IaC
python -m unittest infra.tests.test_terraform_security_and_docs

# Chạy toàn bộ test suite hạ tầng (13+ test suites)
python -m unittest discover -s infra/tests
```

Bộ test tự động xác thực:
- 100% không chứa hardcoded secrets / AWS keys / Tokens.
- Khai báo đầy đủ cờ `sensitive = true` cho các biến nhạy cảm.
- Cấu hình `.gitignore` đầy đủ và không có file state/vars bị track trong Git.
- Cấu trúc tài liệu `infra/README.md` và file điều hướng tương thích `terraform/README.md` hoàn chỉnh.
