# GYM-management Backend

Hướng dẫn nhanh để chạy MySQL bằng Docker và khởi động backend ở môi trường `dev`.

## 1. Cài đặt công cụ

Cài đặt các công cụ sau:

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- IntelliJ IDEA
- JDK 21

Sau khi cài Docker Desktop, mở ứng dụng và chờ Docker Engine chạy hoàn tất.

Kiểm tra Docker:

```powershell
docker --version
docker compose version
```

## 2. Khởi động database bằng Docker

Mở Terminal tại thư mục `backend`:

```powershell
cd backend
docker compose up -d
```

> Lưu ý: Compose đang dùng MySQL 8.0 theo cấu hình mục tiêu; nhánh 8.0 đã hết vòng đời hỗ trợ từ tháng 4/2026, Oracle khuyến nghị 8.4 LTS cho triển khai mới ([ghi chú phát hành MySQL 8.0](https://dev.mysql.com/doc/relnotes/mysql/8.0/en/)). Bản cấu hình trước dùng 8.4, vì vậy không khởi động 8.0 trên volume `mysql_data` đã được 8.4 khởi tạo. MySQL chỉ hỗ trợ hạ từ 8.4 về 8.0 bằng logical dump/load hoặc replication trong điều kiện rollback ([hướng dẫn downgrade](https://dev.mysql.com/doc/refman/8.4/en/downgrading.html)).

Kiểm tra container:

```powershell
docker compose ps
```

Container MySQL cần có trạng thái `healthy` và được ánh xạ:

```text
localhost:3307 -> MySQL container:3306
```

Database sử dụng:

Tạo file .env đặt cùng cấp với file src trong thư mục backend rôì thêm 
biến môi trường mâũ như file .env.example

## 3. Mở backend bằng IntelliJ

1. Mở IntelliJ IDEA.
2. Chọn `Open` và mở thư mục `backend`.
3. Chờ IntelliJ nhận diện file `pom.xml` và tải dependency Maven.
4. Chọn JDK 21 cho project.

## 4. Bật môi trường `dev`

Trong IntelliJ, mở:

```text
Run → Edit Configurations...
```

Chọn cấu hình chạy backend. Tại mục `Environment variables`, thêm:

```text
SPRING_PROFILES_ACTIVE=dev;
```

Vì vậy Flyway sẽ chạy các migration trong `db/migration` và `db/migration-dev`, gồm schema (`V1`, `V2`, `V3`, `V4`), tài khoản demo, dữ liệu mẫu, bộ seed kho/POS `V903` và bộ seed voucher/nhân sự `V904`.

## 5. Chạy ứng dụng

Chạy class `GymManagementApiApplication` bằng nút Run trong IntelliJ.

Khi chạy thành công, Flyway sẽ tạo các bảng nghiệp vụ và bảng:

```text
flyway_schema_history
```

Dữ liệu mẫu local/dev cũng sẽ được tạo. Tài khoản mẫu:

```text
admin@gym.local / 123456          (ADMIN - ACTIVE)
staff@gym.local / 123456          (STAFF - ACTIVE)
staff.active@gym.local / 123456   (STAFF - ACTIVE)
staff.locked@gym.local / 123456   (STAFF - LOCKED)
staff.inactive@gym.local / 123456 (STAFF - INACTIVE)
```

Mã voucher mẫu phục vụ kiểm thử ca biên:

```text
DISCOUNT20_CAP50K  (Giảm 20% tối đa 50k, đơn từ 100k)
FIXED100K_MIN500K  (Giảm 100k cho đơn từ 500k)
EXHAUSTED_VOUCHER  (Đã hết lượt sử dụng)
EXPIRED_VOUCHER    (Đã hết hạn sử dụng)
FUTURE_VOUCHER     (Chưa đến ngày áp dụng)
INACTIVE_VOUCHER   (Trạng thái tạm khóa)
```

## 6. Kiểm tra database

Kiểm tra các bảng trong container:

```powershell
docker exec -it gym-management-mysql mysql -uroot -p123456 -D gym_management -e "SHOW TABLES;"
```

Kiểm tra lịch sử Flyway:

```powershell
docker exec -it gym-management-mysql mysql -uroot -p123456 -D gym_management -e "SELECT installed_rank, version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
```

Kết quả cần có migration schema `V1`, `V2`, `V3`, `V4` (Chặng 4 - tính năng Voucher & bảng `voucher_usages`), migration dữ liệu dev `V900`, `V901`, seed kho/POS `V903` và seed voucher/nhân sự `V904` với `success = 1`.

## 7. Theo dõi và trích xuất log giao dịch đối soát

- **Tự động trích xuất log đối soát**: Phục vụ Tester kiểm tra rollback giao dịch đơn hàng và voucher, chạy 1 lệnh để tự động lọc và lưu file bằng chứng:
  - Trên Windows CMD: `scripts\extract-transaction-logs.bat`
  - Trên PowerShell: `.\scripts\extract-transaction-logs.ps1`
  - Trên Git Bash / Linux: `./scripts/extract-transaction-logs.sh`
  - Kết quả lưu tại: `docs/testing/reports/logs/txn-rollback-YYYYMMDD_HHmmss-summary.log`.
- **Backend log**: xem Run console của IntelliJ hoặc terminal, hoặc file `backend/logs/gym-backend.log`. Mức `DEBUG` của `org.springframework.orm.jpa` và `TRACE` của `org.springframework.transaction` ghi lại các mốc bắt đầu, commit và rollback giao dịch.
- **MySQL**: xem log máy chủ bằng `docker compose logs -f --tail=100 mysql`.
- Xem hướng dẫn chi tiết tại [TESTER_DB_RESET_AND_LOG_GUIDE.md](../docs/testing/TESTER_DB_RESET_AND_LOG_GUIDE.md).

## 8. Dừng database

Dừng container nhưng giữ lại dữ liệu:

```powershell
docker compose stop
```

Xóa container nhưng giữ lại volume dữ liệu:

```powershell
docker compose down
```

Không chạy lệnh sau nếu chưa chắc chắn, vì nó xóa toàn bộ dữ liệu database local:

```powershell
docker compose down -v
```

## 9. Khôi phục dữ liệu mẫu siêu tốc cho Tester (< 3 giây)

Sau mỗi lượt kiểm thử Selenium/Postman làm thay đổi dữ liệu hoặc sau mỗi kịch bản test rollback:

- **Trên Windows CMD (Khuyến nghị cho Tester)**:
  ```cmd
  scripts\reset-test-db.bat
  ```
  *(Có thể double click trực tiếp file `scripts/reset-test-db.bat` trong Windows Explorer)*.

- **Trên PowerShell**:
  ```powershell
  .\scripts\reset-restore-db.ps1
  ```

- **Trên Git Bash / Linux**:
  ```bash
  ./scripts/reset-test-db.sh
  ```

Script tự động nạp toàn bộ Schema Chặng 4 (`V1`–`V4`), dev seed (`V900`, `V901`, `V903`, `V904`), khởi tạo lịch sử Flyway và tự động xác thực các điều kiện tồn kho, nhân sự, voucher và bảng `voucher_usages` chỉ trong 1–3 giây. Nếu cần chạy kiểm tra luồng khởi động Spring Boot cũ, có thể truyền `-UseMaven`.

## Lưu ý

- Database dùng profile `dev` nên tiếp tục được chạy bằng profile `dev`.
- Không dùng dữ liệu mẫu `V900` cho staging hoặc production.
- Không đổi `DB_PORT` sang `3306` nếu Docker Compose vẫn đang dùng port `3307`.
- Nếu port `3307` bị chiếm, cần đổi đồng thời port trong Docker Compose và biến `DB_PORT` của IntelliJ.
