# 🏋️‍♂️ Gym Management System (Hệ Thống Quản Lý Phòng Tập Gym)

Hệ thống quản lý phòng tập gym toàn diện theo kiến trúc **Monorepo**, kết hợp giữa cổng thông tin công khai (Public Landing Page), phân hệ nghiệp vụ dành cho Lễ tân/Nhân viên (Staff Portal) và phân hệ Quản trị vận hành cấp cao (Admin Portal).

Dự án được xây dựng và triển khai theo quy trình Agile/Scrum, tích hợp kiểm thử tự động, chuẩn hóa CI/CD GitHub Actions và vận hành trên hạ tầng điện toán đám mây **AWS (Tuân thủ 100% Free Tier)**.

---

## 📌 Mục Lục
1. [Tổng Quan 14 Use Cases](#-tổng-quan-14-use-cases)
2. [Kiến Trúc Công Nghệ](#-kiến-trúc-công-nghệ)
3. [Cấu Trúc Thư Mục Monorepo](#-cấu-trúc-thư-mục-monorepo)
4. [Hướng Dẫn Cài Đặt & Chạy Cục Bộ (Local)](#-hướng-dẫn-cài-đặt--chạy-cục-bộ-local)
5. [Quy Trình Kiểm Thử (Testing Workflow)](#-quy-trình-kiểm-thử-testing-workflow)
6. [Hạ Tầng AWS & Quản Lý Vòng Đời (Lifecycle Control)](#-hạ-tầng-aws--quản-lý-vòng-đời-lifecycle-control)
7. [Chính Sách Bảo Mật & Quản Lý Cấu Hình](#-chính-sách-bảo-mật--quản-lý-cấu-hình)
8. [Phân Công Trách Nhiệm Dự Án](#-phân-công-trách-nhiệm-dự-án)

---

## 🎯 Tổng Quan 14 Use Cases

* **Phân hệ Công khai (Khách hàng):**
  * `UC01`: Xem thông tin phòng tập, bảng giá gói tập & cơ sở vật chất.
  * `UC02`: Tra cứu lịch tập và danh sách huấn luyện viên (PT).
  * `UC03`: Gửi thông tin tư vấn/liên hệ trực tuyến (Tích hợp thông báo chuông cho Staff/Admin).
* **Phân hệ Lễ tân & Điểm bán (Staff Portal):**
  * `UC04`: Đăng nhập & Xác thực phân quyền (RBAC: Staff, Admin).
  * `UC05`: Quản lý thông tin tài khoản cá nhân & Đổi mật khẩu.
  * `UC06`: Quản lý hồ sơ hội viên (Thêm mới, tra cứu, chỉnh sửa thông tin).
  * `UC07`: Đăng ký, gia hạn & nâng cấp gói tập cho hội viên.
  * `UC08`: Điểm danh (Check-in) hội viên vào phòng tập bằng mã thẻ/SĐT.
  * `UC09`: Bán lẻ tại quầy (Mini POS) - Tự động kiểm tra tồn kho & Rollback giao dịch.
* **Phân hệ Quản trị Hệ thống (Admin Portal):**
  * `UC10`: Quản lý danh sách nhân viên (Cấp tài khoản, phân quyền, khóa/mở trạng thái).
  * `UC11`: Quản lý danh mục gói tập phòng gym (Thêm/Sửa/Khóa gói).
  * `UC12`: Quản lý kho hàng & vật phẩm bán lẻ (Nhập hàng, cảnh báo hết hàng).
  * `UC13`: Quản lý chiến dịch Voucher/Khuyến mãi (Giảm theo %, giảm số tiền cố định, giới hạn lượt dùng).
  * `UC14`: Báo cáo thống kê Dashboard (Doanh thu gói tập, doanh thu quầy POS, xu hướng check-in).

---

## 🛠 Kiến Trúc Công Nghệ

* **Backend:** Java 21, Spring Boot 3, Spring Security, JWT, Spring Data JPA, Flyway Migration, Maven.
* **Frontend:** React 19, Vite, Tailwind CSS, Axios, Lucide Icons, Chart.js.
* **Database:** MySQL 8.4 (Local Docker / AWS RDS Single-AZ).
* **Kiểm thử:** JUnit 5, Mockito, Newman (Postman CLI), Selenium WebDriver.
* **DevOps & Cloud:** Docker Compose, GitHub Actions (CI/CD), Terraform (IaC), AWS ECS (Fargate), AWS ALB, Amazon ECR, Amazon RDS MySQL (`db.t4g.micro`), AWS CloudWatch, DuckDNS (Dynamic DNS Binding).

---

## 📂 Cấu Trúc Thư Mục Monorepo

```text
GYM-management/
├── .github/workflows/    # CI/CD Workflows (ci.yml, cd-prod.yml, infra-control.yml)
├── backend/              # Mã nguồn Spring Boot API & Unit Tests
├── frontend/             # Mã nguồn React SPA (Vite + Tailwind CSS)
├── docs/                 # Tài liệu đặc tả (SRS), kiến trúc hệ thống & hướng dẫn
│   └── testing/          # Tài nguyên kiểm thử tập trung (Postman, Selenium, Reports)
├── infra/                # Mã nguồn Terraform triển khai môi trường Cloud (AWS IaC)
│   ├── modules/          # Các module hạ tầng (vpc, alb, ecs, rds, iam)
│   └── environments/     # Cấu hình độc lập (staging, prod)
├── scripts/              # Các kịch bản tự động hóa, reset database, kiểm thử
├── .env.example          # Mẫu khai báo biến môi trường (Không chứa thông tin bí mật)
├── docker-compose.yml    # Cấu hình khởi chạy toàn bộ dịch vụ cục bộ
└── README.md

🚀 Hướng Dẫn Cài Đặt & Chạy Cục Bộ (Local)
1. Yêu cầu môi trường
Git

Docker & Docker Compose (v20.10 trở lên)

Java 21 & Maven 3.9+ (Nếu chạy Backend độc lập)

Node.js 20+ & npm 10+ (Nếu chạy Frontend độc lập)

2. Thiết lập Biến môi trường
Sao chép file mẫu và cấu hình các giá trị phù hợp trên máy phát triển cá nhân:

Bash
cp .env.example .env
(Lưu ý: Không bao giờ commit file .env chứa mật khẩu thật lên Git).

3. Khởi chạy toàn bộ hệ thống bằng Docker Compose
Tại thư mục gốc của dự án, thực hiện:

Bash
# Build và khởi chạy các container dưới nền
docker compose up -d --build

# Kiểm tra trạng thái hoạt động của các dịch vụ
docker compose ps
Frontend: http://localhost:5173

Backend API: http://localhost:8080/api

Swagger/OpenAPI (nếu bật): http://localhost:8080/swagger-ui.html

4. Dữ liệu thử nghiệm (Seed Data)
Hệ thống sử dụng Flyway để tự động khởi tạo bảng và nạp dữ liệu mẫu ban đầu:

Tài khoản quản trị và nhân viên mẫu được thiết lập tự động thông qua file migration môi trường dev/staging.

Thông tin cấu hình tài khoản kiểm thử nội bộ được lưu trữ tại tài liệu hướng dẫn nội bộ docs/SETUP_GUIDE.md.

🧪 Quy Trình Kiểm Thử (Testing Workflow)
Dự án áp dụng mô hình Shift-Left Testing và kiểm soát chất lượng qua từng lớp:

Unit Testing (Tầng Service & Controller):

Được quản lý trong thư mục backend/src/test/java/.

Sử dụng JUnit 5 kết hợp Mockito (@Mock, @InjectMocks) kiểm thử độc lập.

Lệnh thực thi:

Bash
cd backend
mvn clean test
Báo cáo kiểm thử XML được xuất tự động vào target/surefire-reports/.

API Testing (Postman Collections):

Toàn bộ kịch bản kiểm thử API (UC04 đến UC14) được lưu trữ tại docs/testing/postman/.

Chạy tự động qua dòng lệnh bằng Newman:

Bash
newman run docs/testing/postman/Gym_API_Collection.json -e docs/testing/postman/gym-environment.json
E2E Testing (Selenium):

Mã nguồn kiểm thử giao diện tự động đặt tại docs/testing/selenium/.

Bao phủ các luồng trọng yếu: gửi thông tin liên hệ (UC03), bán hàng trừ tồn kho (UC09), áp voucher & báo cáo doanh thu (UC13, UC14).

☁️ Hạ Tầng AWS & Quản Lý Vòng Đời (Lifecycle Control)
Hạ tầng được triển khai theo mô hình Zero-Cost Architecture, khai thác tối đa hạn mức AWS Free Tier (12 tháng):

Tính toán: AWS ECS Fargate với mức tinh chỉnh siêu nhẹ (0.25 vCPU, 512 MiB RAM).

Cơ sở dữ liệu: Amazon RDS MySQL Single-AZ (db.t4g.micro, 20 GB SSD gp2, backup 7 ngày, không dùng NAT Gateway).

Bộ cân bằng tải: Application Load Balancer (ALB) định tuyến HTTP/HTTPS.

Cơ chế Quản lý Vòng đời & Tiết kiệm Chi phí (infra-control.yml)
Workflow trên GitHub Actions cung cấp 3 hành động quản lý chi phí linh hoạt:

Pause: Thu nhỏ (Scale) các service container về 0 task. Giữ ALB và RDS ở trạng thái chạy để khôi phục nhanh dưới 1 phút.

Hibernate (Chi phí về $0): Hủy bỏ ALB và container services; gửi lệnh stop-db-instance tạm dừng RDS mà vẫn giữ nguyên 100% dung lượng ổ đĩa 20 GB (Không mất dữ liệu giao dịch).

Resume: Đánh thức RDS về trạng thái available, tái khởi tạo các dịch vụ container và tự động kích hoạt Dynamic DNS Binding trỏ domain DuckDNS về IP mới.

🔒 Chính Sách Bảo Mật & Quản Lý Cấu Hình
Tuyệt đối không lưu Credential trên Git: Mọi khóa truy cập (Access Keys), Secret Keys, Chuỗi JWT Secret, Mật khẩu Database đều được quản lý qua GitHub Secrets và AWS Systems Manager (SSM) Parameter Store.

Phân quyền truy cập cơ sở dữ liệu: Cơ sở dữ liệu Production nằm trong Private Database Subnet, hoàn toàn không mở ra Internet và chỉ tiếp nhận kết nối qua cổng 3306 từ Security Group của container Backend.

Cấu hình Log tối ưu: Môi trường Production tắt toàn bộ câu lệnh debug SQL thô (show-sql: false) nhằm bảo vệ dữ liệu nhạy cảm của khách hàng và duy trì mức ghi log trong trần 5 GB miễn phí của CloudWatch Logs.  
