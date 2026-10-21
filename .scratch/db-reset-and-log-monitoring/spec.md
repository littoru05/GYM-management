# Spec: [SUB - 3] Giám sát Log Giao dịch & Tối ưu Script Reset Database Phục vụ Tester Đối soát

## Overview
Cung cấp giải pháp toàn diện cho Tester (Thanh) và đội ngũ phát triển:
1. Script reset database siêu tốc (< 3 giây) nạp đầy đủ schema Chặng 4 (`V1`, `V2`, `V3`, `V4`) và dev seed data (`V900`, `V901`, `V903`, `V904`), tự động đồng bộ bảng `flyway_schema_history`.
2. Hỗ trợ đa nền tảng với 1 lệnh thực thi duy nhất:
   - `scripts/reset-test-db.bat` cho Windows CMD (1-click, không lo vướng PowerShell ExecutionPolicy).
   - `scripts/reset-test-db.sh` cho Bash / Linux / macOS / Git Bash.
   - `scripts/reset-restore-db.ps1` được tối ưu hóa đồng bộ.
3. Bộ công cụ giám sát & trích xuất log đối soát giao dịch:
   - Cấu hình backend dev ghi log file ra `logs/gym-backend.log`.
   - `scripts/extract-transaction-logs.bat`, `.sh`, `.ps1` trích xuất log từ Docker container (`gym-management-backend`) hoặc log file local.
   - Lọc các sự kiện trọng tâm: JPA Transaction Manager, Rollback, POS Checkout, Voucher Usage, Exception.
   - Hiển thị thống kê trên console và xuất file bằng chứng ra `docs/testing/reports/logs/`.
4. Tài liệu hướng dẫn chi tiết cho Tester Thanh tại `docs/testing/TESTER_DB_RESET_AND_LOG_GUIDE.md` và cập nhật `backend/README.md`.

## Definition of Done (DoD)
- Script reset DB chạy thành công với 1 câu lệnh, hoàn tất nạp schema V4 và seed data chỉ trong vài giây.
- Các điều kiện kiểm tra toàn vẹn (products, staff, vouchers, voucher_usages, flyway) đều pass.
- Script trích xuất log hoạt động chính xác cả môi trường Docker và môi trường Local, tự tạo thư mục và xuất file có timestamp.
- Tài liệu hướng dẫn đối soát rõ ràng, dễ hiểu cho Tester.
