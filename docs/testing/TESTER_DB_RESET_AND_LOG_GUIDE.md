# Hướng Dẫn Reset Database Siêu Tốc & Giám Sát Log Đối Soát Giao Dịch
> Dành cho: **Tester (Thanh)** và Đội ngũ Kiểm thử / DevOps  
> Phạm vi: **Chặng 4 & Sprint 3.4** (Kiểm thử Rollback Đơn hàng POS, Quản lý Voucher & Giao dịch)

---

## 1. Mục Tiêu & Lợi Ích

1. **Reset Database 1 chạm siêu tốc (< 3 giây)**: Làm mới toàn bộ cơ sở dữ liệu về trạng thái chuẩn chỉ bằng **1 câu lệnh duy nhất** (không cần đợi khởi động Spring Boot 15–30s). Phục vụ Tester chạy lặp đi lặp lại hàng chục test cases mà không tốn thời gian chờ đợi.
2. **Đồng bộ toàn diện Schema Chặng 4**: Nạp tự động toàn bộ schema migration `V1`, `V2`, `V3`, `V4` (bảng `voucher_usages`, các thuộc tính mở rộng voucher) và các bộ seed dữ liệu dev `V900`, `V901`, `V903`, `V904`.
3. **Trích xuất Log Đối soát Tự động**: Tự động lọc và kết xuất các mốc giao dịch (bắt đầu, commit, rollback), ngoại lệ hết hàng tồn kho, và lịch sử voucher ra file log có timestamp để đính kèm làm minh chứng trong báo cáo kiểm thử.

---

## 2. Hướng Dẫn Reset Database (1 Câu Lệnh)

### 2.1. Cách thực hiện trên Windows (Khuyến nghị cho Tester)

Mở cửa sổ **Command Prompt (cmd)** hoặc PowerShell tại thư mục gốc dự án, chạy:

```cmd
scripts\reset-test-db.bat
```

> **Mẹo**: Tester cũng có thể **double-click** trực tiếp vào file `scripts/reset-test-db.bat` trong Windows File Explorer. Script sẽ tự động chạy và đóng khi hoàn tất.

### 2.2. Cách thực hiện trên PowerShell

```powershell
.\scripts\reset-restore-db.ps1
```

*(Script tự động chạy chế độ siêu tốc qua MySQL socket container. Nếu muốn kiểm tra quy trình Spring Boot Maven cũ, có thể thêm tham số `-UseMaven`).*

### 2.3. Cách thực hiện trên Linux / macOS / Git Bash

```bash
./scripts/reset-test-db.sh
```

---

## 3. Dữ Liệu Được Tự Động Thiết Lập & Xác Thực

Sau mỗi lần reset thành công, cơ sở dữ liệu `gym_management` luôn được đảm bảo các điều kiện sau:

| Đối tượng | Chi tiết dữ liệu sẵn sàng cho Tester | Trạng thái xác thực |
| :--- | :--- | :--- |
| **Schema Chặng 4** | Bảng `voucher_usages` và các cột `applicable_to_all`, `applicable_membership_ids` trên `promotions` | Đã kiểm tra cấu trúc |
| **Flyway History** | Đầy đủ 8 migration (`V1`, `V2`, `V3`, `V4`, `V900`, `V901`, `V903`, `V904`) | `success = 1` |
| **Sản phẩm POS** | **10 sản phẩm mẫu**: 4 sản phẩm dồi dào (50-100 cái), 5 sản phẩm cận kho ($\le$ min_stock), 2 sản phẩm còn 1-2 cái (SUP-WHEY-CHOCO-001: 2 cái, SUP-CREATINE-100-001: 1 cái), 1 sản phẩm hết hàng (DRK-ELECTRO-LEMON-001: 0 cái), 1 sản phẩm ngưng bán | Khớp 100% |
| **Tài khoản Nhân sự** | `admin@gym.local` (ADMIN), `staff@gym.local` (STAFF - ACTIVE), `staff.active@gym.local` (ACTIVE), `staff.locked@gym.local` (LOCKED), `staff.inactive@gym.local` (INACTIVE). Mật khẩu: `123456` | Khớp 100% |
| **Voucher Kiểm thử** | **6 mã voucher ca biên**: `DISCOUNT20_CAP50K` (giảm 20% max 50k, min 100k), `FIXED100K_MIN500K` (giảm 100k min 500k), `EXHAUSTED_VOUCHER` (đã hết lượt), `EXPIRED_VOUCHER` (đã hết hạn), `FUTURE_VOUCHER` (chưa đến ngày), `INACTIVE_VOUCHER` (đang tạm khóa) | Khớp 100% |

---

## 4. Hướng Dẫn Trích Xuất Log Đối Soát Giao Dịch & Rollback

Khi Tester chạy kịch bản kiểm tra Rollback đơn hàng hoặc Voucher và cần bằng chứng đối soát log hệ thống:

### 4.1. Câu lệnh trích xuất nhanh

Trên Windows CMD:
```cmd
scripts\extract-transaction-logs.bat
```

Trên PowerShell:
```powershell
.\scripts\extract-transaction-logs.ps1
```

Trên Git Bash / Linux:
```bash
./scripts/extract-transaction-logs.sh
```

### 4.2. Cơ chế hoạt động của Script Trích xuất
* **Tự động nhận diện môi trường**: Tự động lấy log từ Docker container `gym-management-backend` (nếu đang chạy Docker Compose) hoặc đọc trực tiếp từ file log local `backend/logs/gym-backend.log`.
* **Bộ lọc thông minh**: Lọc chính xác các sự kiện trọng yếu:
  - Bắt đầu giao dịch: `Creating new transaction`
  - Rollback giao dịch: `Initiating transaction rollback`, `Rolling back JPA transaction`
  - Bán hàng POS: `Bắt đầu xử lý POS Checkout`, `Tạo đơn hàng POS thành công`
  - Ngoại lệ tồn kho: `InsufficientStockException`, `không đủ tồn kho`
  - Khuyến mãi/Voucher: `validateVoucher`, `voucher_usages`, `Đã lưu lịch sử sử dụng voucher`
  - Lỗi phát sinh: `ERROR`, `Exception`
* **Hiển thị thống kê tổng hợp**: In ngay số lượng rollback, số lỗi tồn kho, số đơn hàng lên màn hình console.
* **Lưu file bằng chứng**: Tự động lưu ra thư mục `docs/testing/reports/logs/txn-rollback-YYYYMMDD_HHmmss-summary.log` kèm header tóm tắt.

### 4.3. Các tùy chọn nâng cao khi cần
* Xem nhiều dòng hơn (mặc định 1000 dòng gần nhất):
  ```cmd
  scripts\extract-transaction-logs.bat --lines 3000
  ```
* Xuất toàn bộ log thô (không lọc từ khóa):
  ```cmd
  scripts\extract-transaction-logs.bat --all
  ```

---

## 5. Kịch Bản Mẫu Kiểm Thử Đối Soát (Dành Cho Tester Thanh)

### 📌 Kịch Bản 1: Kiểm Thử Rollback Tồn Kho Khi Đơn Hàng POS Thất Bại (UC09)
1. **Bước 1**: Chạy reset DB:
   ```cmd
   scripts\reset-test-db.bat
   ```
   *(Sản phẩm `SUP-WHEY-CHOCO-001` đang có tồn kho = 2)*.
2. **Bước 2**: Thực hiện request POST `/api/pos/orders` với 2 sản phẩm:
   - Sản phẩm A: `SUP-WHEY-CHOCO-001` (ID: xem DB, yêu cầu mua: 1 cái $\rightarrow$ hợp lệ).
   - Sản phẩm B: `DRK-ELECTRO-LEMON-001` (yêu cầu mua: 1 cái $\rightarrow$ sản phẩm này tồn kho = 0).
3. **Bước 3**: Quan sát phản hồi API:
   - API trả về HTTP 400 Bad Request kèm thông báo `InsufficientStockException: Sản phẩm ... không đủ tồn kho`.
4. **Bước 4**: Kiểm tra DB xem tồn kho Sản phẩm A có bị trừ nhầm không:
   - Chạy truy vấn hoặc kiểm tra giao diện: Tồn kho của `SUP-WHEY-CHOCO-001` **vẫn phải giữ nguyên là 2** (Rollback thành công).
5. **Bước 5**: Trích xuất log đối soát:
   ```cmd
   scripts\extract-transaction-logs.bat
   ```
   - Mở file log vừa tạo trong `docs/testing/reports/logs/`.
   - Tìm thấy các dòng minh chứng:
     ```text
     JpaTransactionManager : Creating new transaction with name [com.example.GYM_management_api.services.impl.PosOrderServiceImpl.createOrder]
     PosOrderServiceImpl   : Sản phẩm ID=... 'Nước điện giải Lemon' không đủ tồn kho (Hiện có: 0, Yêu cầu: 1)
     JpaTransactionManager : Initiating transaction rollback
     JpaTransactionManager : Rolling back JPA transaction on EntityManager
     ```

---

### 📌 Kịch Bản 2: Kiểm Thử Rollback Voucher Khi Kích Hoạt Gói Tập Thất Bại (UC10/UC14)
1. **Bước 1**: Reset DB:
   ```cmd
   scripts\reset-test-db.bat
   ```
2. **Bước 2**: Gọi API tạo đăng ký gói tập kèm mã voucher `DISCOUNT20_CAP50K`, nhưng cố tình truyền dữ liệu hội viên không hợp lệ hoặc lỗi phương thức thanh toán.
3. **Bước 3**: Quan sát phản hồi:
   - Request bị từ chối với HTTP 400.
4. **Bước 4**: Kiểm tra bảng voucher và log đối soát:
   - Số lượt dùng `used_count` của voucher `DISCOUNT20_CAP50K` trong bảng `promotions` **vẫn là 0**.
   - Bảng `voucher_usages` **hoàn toàn không có bản ghi mới** liên quan đến giao dịch lỗi.
   - Chạy `scripts\extract-transaction-logs.bat` để lưu log xác nhận transaction rollback của Spring Data JPA.

---

## 6. Hỗ Trợ Xử Lý Sự Cố Thường Gặp

* **Lỗi Docker daemon not running**: Khởi động ứng dụng Docker Desktop trên Windows và đợi Docker báo trạng thái "Engine running" màu xanh rồi chạy lại script.
* **Lỗi Container gym-management-mysql not found**: Chạy lệnh khởi động container từ thư mục gốc dự án:
  ```cmd
  docker compose up -d mysql
  ```
  Sau đó chạy lại `scripts\reset-test-db.bat`.
* **Cần log từ file local thay vì docker**:
  ```cmd
  scripts\extract-transaction-logs.bat -LocalOnly
  ```
