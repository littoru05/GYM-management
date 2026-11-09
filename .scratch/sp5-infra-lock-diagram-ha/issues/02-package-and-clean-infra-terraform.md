# 02: Package & Clean Infrastructure Terraform Codebase

**What to build:** Rà soát, kiểm toán an ninh và làm sạch thư mục mã nguồn hạ tầng `infra/` (`infra/bootstrap/`, `infra/environments/prod`, `infra/environments/staging`, `infra/modules/*`). Đảm bảo 100% không chứa hardcoded secrets (AWS Keys, DB passwords, JWT tokens), các biến nhạy cảm được cấu hình `sensitive = true`, và các file nhạy cảm/tạm thời được loại trừ bởi `.gitignore`. Soạn thảo tài liệu hướng dẫn vận hành hạ tầng toàn diện `infra/README.md` mô tả chi tiết các câu lệnh `init`, `plan`, `apply`, `destroy` cho cả Staging và Production, kèm theo file điều hướng tương thích `terraform/README.md`. Bổ sung bộ test tự động xác thực an ninh mã nguồn và tính đầy đủ của tài liệu.

Blocked by: none

Status: ready-for-review

- [x] Thực hiện kiểm toán an ninh tự động trên toàn bộ các file `.tf`, `.tfvars`, scripts trong thư mục `infra/`, đảm bảo không chứa bất kỳ secret/khóa nhạy cảm dạng plaintext nào (sử dụng SSM Parameter Store hoặc biến môi trường `TF_VAR_*`).
- [x] Rà soát và cập nhật file `infra/.gitignore` và root `.gitignore` để bảo đảm không lưu vết file `.terraform/`, `*.tfstate`, `*.tfvars`, `*.backup`, lock files cục bộ hoặc credentials.
- [x] Soạn thảo tài liệu hướng dẫn vận hành chi tiết `infra/README.md` bao gồm:
  - Tổng quan kiến trúc IaC và sơ đồ thư mục module.
  - Yêu cầu tiên quyết về công cụ (Terraform CLI v1.5+, AWS CLI v2) và quyền hạn IAM.
  - Hướng dẫn thiết lập Remote State Backend (`infra/bootstrap`).
  - Hướng dẫn triển khai môi trường Staging (`infra/environments/staging` -> init, plan, apply).
  - Hướng dẫn triển khai môi trường Production (`infra/environments/prod` -> init, plan, apply).
  - Hướng dẫn cấu hình biến nhạy cảm và quản lý state an toàn.
  - Hướng dẫn tiêu hủy tài nguyên có kiểm soát (`terraform destroy`).
- [x] Tạo thư mục `terraform/` và file điều hướng `terraform/README.md` trỏ về `infra/` để tương thích hoàn toàn với yêu cầu đường dẫn của đề bài mà không làm phá vỡ các đường dẫn CI/CD hiện có.
- [x] Xây dựng test suite `infra/tests/test_terraform_security_and_docs.py` để tự động kiểm tra phát hiện rò rỉ secret, kiểm tra flag `sensitive = true` và xác thực cấu trúc tài liệu `README.md`.
