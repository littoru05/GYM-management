# 01: [SUB - 3] Giám sát Log Giao dịch & Tối ưu Script Reset Database Phục vụ Tester Đối soát

Status: resolved
Type: task

## Context
Tester (Thanh) cần chạy lặp lại các kịch bản kiểm thử cho nghiệp vụ Rollback giao dịch đơn hàng và áp dụng/hoàn voucher.
Hiện tại việc reset DB thông qua Spring Boot Maven mất 15-30s và thiếu script 1-click native trên Windows CMD (`.bat`) cũng như công cụ trích xuất log đối soát.

## Requirements
1. **Script Reset DB**:
   - `scripts/reset-test-db.bat` cho Windows CMD.
   - `scripts/reset-test-db.sh` cho Bash/Linux/macOS/Git Bash.
   - Cập nhật `scripts/reset-restore-db.ps1`.
   - Nạp toàn bộ schema Chặng 4 (`V1`, `V2`, `V3`, `V4`), dev seed (`V900`, `V901`, `V903`, `V904`), và cập nhật `flyway_schema_history`.
   - Thời gian chạy < 3 giây trực tiếp qua MySQL container CLI.
   - Chạy 1 câu lệnh không cần prompt xác nhận.
   - Kiểm tra assertions: 10 sản phẩm, 3 nhân sự, 6 voucher, bảng `voucher_usages` và các cột mới của Chặng 4.
2. **Log Monitoring & Extraction**:
   - Cập nhật `application-dev.yaml` thêm `logging.file.name: logs/gym-backend.log`.
   - Viết `scripts/extract-transaction-logs.bat`, `.sh`, `.ps1` để tự động lọc log transaction/rollback/voucher/order từ Docker container hoặc file log local.
   - Xuất file kết quả có timestamp vào `docs/testing/reports/logs/`.
3. **Documentation**:
   - Viết `docs/testing/TESTER_DB_RESET_AND_LOG_GUIDE.md` và cập nhật `backend/README.md`.

## Answer
- Đã triển khai bộ script reset database siêu tốc (< 3 giây) qua kết nối socket trực tiếp vào MySQL container:
  - `scripts/reset-test-db.bat`: Script native Windows Batch cho Tester (1 chạm không vướng execution policy).
  - `scripts/reset-test-db.sh`: Script cho Linux/macOS/Git Bash.
  - `scripts/reset-restore-db.ps1`: Script PowerShell tối ưu hóa, hỗ trợ cờ `-UseMaven` nếu cần fallback.
  - Tự động nạp schema Chặng 4 (`V1`-`V4`), dev seed data (`V900`, `V901`, `V903`, `V904`), tạo bảng `flyway_schema_history` và kiểm tra toàn vẹn (products, staff, vouchers, voucher_usages).
- Đã triển khai bộ script giám sát & trích xuất log giao dịch đối soát:
  - `scripts/extract-transaction-logs.bat`, `extract-transaction-logs.sh`, `extract-transaction-logs.ps1`.
  - Tự động nhận diện container Docker `gym-management-backend` hoặc file log local `backend/logs/gym-backend.log`.
  - Phân tích và thống kê số lượt rollback, lỗi tồn kho, đơn hàng POS, sự kiện voucher.
  - Xuất báo cáo log có timestamp vào `docs/testing/reports/logs/`.
- Đã thêm hướng dẫn đầy đủ tại `docs/testing/TESTER_DB_RESET_AND_LOG_GUIDE.md` và cập nhật `backend/README.md`.
- Toàn bộ 118 unit tests của backend đều vượt qua.
