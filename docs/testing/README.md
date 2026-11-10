# 🧪 TÀI LIỆU HƯỚNG DẪN THIẾT LẬP VÀ CHẠY KIỂM THỬ HỆ THỐNG
> **Dự án:** Hệ thống Quản lý Phòng tập Gym (PowerFit Gym Management System)  
> **Học phần:** Project 1 (Lớp: N04 - Nhóm: 08)  
> **Giảng viên hướng dẫn:** TS. Nguyễn Trọng Phúc  
> **Đơn vị đào tạo:** Khoa Công nghệ Thông tin - Trường Đại học Giao thông Vận tải  
> **Nhóm thực hiện:** Nhóm 08 (Tester: Nguyễn Văn Thanh - MSV: 231230903)  
> **Công cụ chính thức:** Postman Desktop / Newman CLI & Selenium IDE Extension trên trình duyệt Microsoft Edge  
> **Phiên bản hoàn thiện:** Sprint 5 - Nghiệm thu và Đóng gói toàn bộ tài nguyên kiểm thử (KAN-250 / KAN-264)  

---

## 📑 MỤC LỤC
1. [Tổng Quan Bộ Tài Nguyên Kiểm Thử](#1-tổng-quan-bộ-tài-nguyên-kiểm-thử)
2. [Cấu Trúc Thư Mục Chuẩn Mực `docs/testing/`](#2-cấu-trúc-thư-mục-chuẩn-mực-docstesting)
3. [Yêu Cầu Môi Trường & Công Cụ (Prerequisites)](#3-yêu-cầu-môi-trường--công-cụ-prerequisites)
4. [Hướng Dẫn Kiểm Thử API Bằng Postman & Newman CLI](#4-hướng-dẫn-kiểm-thử-api-bằng-postman--newman-cli)
   - [4.1. Thực thi trên ứng dụng Postman Desktop (Giao diện)](#41-thực-thi-trên-ứng-dụng-postman-desktop-giao-diện)
   - [4.2. Thực thi dòng lệnh qua Newman CLI & Xuất báo cáo HTML](#42-thực-thi-dòng-lệnh-qua-newman-cli--xuất-báo-cáo-html)
5. [Hướng Dẫn Kiểm Thử Giao Diện E2E Bằng Selenium IDE Trên Microsoft Edge](#5-hướng-dẫn-kiểm-thử-giao-diện-e2e-bằng-selenium-ide-trên-microsoft-edge)
   - [5.1. Cài đặt Selenium IDE Extension trên Microsoft Edge](#51-cài-đặt-selenium-ide-extension-trên-microsoft-edge)
   - [5.2. Danh mục kịch bản Selenium IDE (.side) sẵn có](#52-danh-mục-kịch-bản-selenium-ide-side-sẵn-có)
   - [5.3. Các bước mở và chạy kịch bản trên Microsoft Edge](#53-các-bước-mở-và-chạy-kịch-bản-trên-microsoft-edge)
   - [5.4. Đọc kết quả và phân tích Log kiểm thử](#54-đọc-kết-quả-và-phân-tích-log-kiểm-thử)
6. [Quy Trình Chuẩn Bị Môi Trường & Reset CSDL 1-Chạm](#6-quy-trình-chuẩn-bị-môi-trường--reset-csdl-1-chạm)
7. [Danh Mục Báo Cáo Minh Chứng Nghiệm Thu](#7-danh-mục-báo-cáo-minh-chứng-nghiệm-thu)
8. [Khắc Phục Sự Cố Thường Gặp (Troubleshooting)](#8-khắc-phục-sự-cố-thường-gặp-troubleshooting)

---

## 1. TỔNG QUAN BỘ TÀI NGUYÊN KIỂM THỬ

Thư mục `docs/testing/` là trung tâm lưu trữ toàn bộ tài sản kiểm thử (Test Assets) của dự án PowerFit Gym, phục vụ việc đánh giá, nghiệm thu và chạy lại kịch bản (Reproducibility) bất cứ lúc nào:

* **Quy mô bộ kiểm thử**:
  - **482 Test cases** tổng thể (được đồng bộ và đối soát 1:1 trong [Testcases - Group-08.xlsx](file:///c:/Users/tn180/OneDrive/Desktop/GYM-management-develop/excel/Testcases%20-%20Group-08.xlsx)).
  - **260 ca kiểm thử REST API**: Tự động hóa 100% bằng Postman Collections & Newman CLI.
  - **222 ca kiểm thử Giao diện (UI)**: Bao gồm 154 ca tự động hóa bằng Selenium IDE trên Microsoft Edge và 68 ca kiểm thử thủ công (Manual UI/UX).
  - **Tỷ lệ Pass Rate đạt 98.55%** (475/482 ca Passed; 7 Defect tiêu biểu đã phát hiện, xử lý và xác nhận fix triệt để).
* **Đối tượng sử dụng**:
  - **Giảng viên hướng dẫn & Hội đồng nghiệm thu**: Dễ dàng import bộ sưu tập Postman hoặc nạp file `.side` trên trình duyệt Microsoft Edge, bấm chạy 1-Click để kiểm chứng tính năng thời gian thực.
  - **Đội ngũ Phát triển (Developers)**: Chạy kiểm thử hồi quy (Regression Testing) tại máy local hoặc môi trường AWS Staging trước khi hợp nhất mã nguồn.
  - **Đội ngũ Kiểm thử (QA/Testers)**: Quản lý phiên bản, đối soát log giao dịch CSDL và trích xuất minh chứng nghiệm thu.

---

## 2. CẤU TRÚC THƯ MỤC CHUẨN MỰC `docs/testing/`

Cây thư mục được chuẩn hóa khoa học và phân định độc lập:

```text
docs/testing/
├── README.md                                      # [Tài liệu này] Hướng dẫn thiết lập và chạy toàn bộ kịch bản test
├── TESTER_DB_RESET_AND_LOG_GUIDE.md               # Sổ tay reset CSDL siêu tốc & trích xuất log đối soát giao dịch
│
├── postman/                                       # BỘ SƯU TẬP KIỂM THỬ API RESTFUL (POSTMAN)
│   ├── GYM_Local.postman_environment.json         # Biến môi trường chạy cục bộ (base_url: http://localhost:8080)
│   ├── GYM_Server_DevOps.postman_environment.json # Biến môi trường chạy AWS Staging (base_url: AWS ALB)
│   ├── SP_ALL_GYM_Postman_Collection.json         # ⭐ MASTER COLLECTION: Gộp toàn bộ 260 API của cả 12 Module (1-click run)
│   ├── SP03.1_GYM_Postman_Collection.json         # Kịch bản API SP01 (Public Web, Gói tập, Đăng ký, Checkin)
│   ├── SP03.2_GYM_Postman_Collection.json         # Kịch bản API SP02 (Quản lý Hội viên, Thẻ tập, Lịch tập)
│   ├── SP03.3.1_GYM_Postman_Collection.json       # Kịch bản API SP03.1 (Quản lý Kho hàng & Phiếu nhập kho)
│   ├── SP03.3.2_GYM_Postman_Collection.json       # Kịch bản API SP03.2 (Bán hàng POS & Transaction Rollback)
│   ├── SP03.4_GYM_Postman_Collection.json         # Kịch bản API SP04 (Nhân sự UC10, Voucher UC13, Báo cáo UC14)
│   ├── SP03.4.1_GYM_Postman_Collection.json       # Kịch bản API SP04.1 (Chuông thông báo Realtime Leads UC03)
│   └── SP3-UPDATE_GYM_Postman_Collection_(UC09,UC12,UC01).json # Kịch bản bổ sung KAN-285 (Ảnh sản phẩm & form tư vấn)
│
├── selenium/                                      # BỘ KỊCH BẢN KIỂM THỬ GIAO DIỆN TỰ ĐỘNG (SELENIUM IDE)
│   ├── SP_ALL_GYM_Selenium_Suite.side             # ⭐ MASTER UI SUITE: Gộp toàn bộ 222 kịch bản UI của cả 12 Module
│   ├── SP03.1_GYM_Public_UC01_to_UC04.side        # Kịch bản UI SP01 (Landing Page, Gói tập, Đăng ký, Form liên hệ)
│   ├── SP03.2_GYM_Management_UC06_UC08_UC11.side  # Kịch bản UI SP02 (Quản lý gói tập, Hội viên, Điểm danh quầy)
│   ├── SP03.3.1GYM_Management_UC12_Inventory.side # Kịch bản UI SP03.1 (Kho hàng, Danh mục, Nhập kho)
│   ├── SP03.3.2GYM_Management_UC09_POS.side       # Kịch bản UI SP03.2 (Bán hàng quầy POS, Giỏ hàng, Hóa đơn nhiệt)
│   ├── SP03.4_GYM_Management_UC10_UC13_UC14_Admin.side # Kịch bản UI SP04 (Admin: Nhân sự, Voucher, Dashboard doanh thu)
│   ├── SP03.4.1_GYM_Notification_UC03.side        # Kịch bản UI SP04.1 (Chuông thông báo Realtime & Modal chi tiết)
│   └── SP03_UPDATE_GYM_Management_UC01_UC09_UC12.side # Kịch bản cập nhật ảnh sản phẩm POS & Leads KAN-285
│
└── reports/                                       # MINH CHỨNG NGHIỆM THU CHI TIẾT (.DOCX & .PDF)
    ├── Postman/                                   # Minh chứng kết quả kiểm thử API Postman
    │   ├── SP03.1_Minh_chung_Kiem_thu_API_Postman.docx
    │   ├── SP03.2_Minh_chung_Kiem_thu_API_Postman.docx
    │   ├── SP03.3.1_Minh_chung_Kiem_thu_API_Postman.docx
    │   ├── SP03.3.2_Minh_chung_Kiem_thu_API_Postman.docx
    │   ├── SP03.4_Minh_chung_Kiem_thu_API_Postman.docx
    │   ├── SP03.4.1_Minh_chung_Kiem_thu_API_Postman.docx
    │   └── SP03_Update_Minh_Chung_Kiem_thu_Api_Postman.docx
    │
    ├── Selenium/                                  # Minh chứng kết quả kiểm thử giao diện Selenium UI
    │   ├── SP03.1_Minh_chung_Kiem_thu_Selenium_UI_Public.docx
    │   ├── SP03.2_Minh_chung_Kiem_thu_Selenium_UI.docx
    │   ├── SP03.3.1_Minh_chung_Kiem_thu_Selenium_UI.docx
    │   ├── SP03.3.2_Minh_chung_Kiem_thu_Selenium_UI.docx
    │   ├── SP03.4_Minh_chung_Kiem_thu_Selenium_UI.docx
    │   ├── SP03.4.1_Minh_chung_Kiem_thu_Selenium_UI.docx
    │   └── SP03_Update_Minh_Chung_Kiem_thu_Selenium_UI.docx
    │
    └── Summary/                                   # Báo cáo tổng hợp kiểm thử toàn diện
        └── Test_Summary_Report_G04.pdf
```

---

## 3. YÊU CẦU MÔI TRƯỜNG & CÔNG CỤ (PREREQUISITES)

Để chạy trọn vẹn và tái hiện đúng kết quả kiểm thử, máy tính cần cài đặt các công cụ sau:

| Công cụ / Nền tảng | Phiên bản khuyến nghị | Mục đích sử dụng | Hướng dẫn tải / Cài đặt |
| :--- | :--- | :--- | :--- |
| **Postman Desktop** | v10.x hoặc v11.x | Mở kịch bản và chạy Postman Collection Runner | [Tải Postman chính thức](https://www.postman.com/downloads/) |
| **Microsoft Edge Browser** | Phiên bản 120+ | Trình duyệt chạy kịch bản kiểm thử giao diện | Có sẵn trên Windows (hoặc tải [Edge](https://www.microsoft.com/edge)) |
| **Selenium IDE Extension** | v3.17.x hoặc mới nhất | Tiện ích mở rộng trên Edge để chạy file `.side` | Cài đặt trực tiếp từ Microsoft Edge Add-ons |
| **Node.js & npm** | Node.js v18.x hoặc v20.x LTS | Tùy chọn chạy Newman CLI và build Frontend | [Tải Node.js LTS](https://nodejs.org/) |
| **Java OpenJDK** | JDK 21 | Chạy máy chủ Backend Spring Boot | Cài đặt JDK 21 (Temurin / Oracle) |
| **MySQL Database** | MySQL 8.0.x (Port 3306) | Cơ sở dữ liệu chính của hệ thống | Sử dụng local MySQL hoặc Docker container |

---

## 4. HƯỚNG DẪN KIỂM THỬ API BẰNG POSTMAN & NEWMAN CLI

### 4.1. Thực thi trên ứng dụng Postman Desktop (Giao diện)

#### Bước 1: Khởi động hệ thống Backend
Đảm bảo máy chủ Backend Spring Boot đang chạy ổn định tại cổng `8080`:
```bash
# Kiểm tra nhanh endpoint công khai
curl http://localhost:8080/api/public/packages
```

#### Bước 2: Import Collection & Environment vào Postman
1. Mở ứng dụng **Postman Desktop**.
2. Nhấp vào nút **Import** ở góc trên bên trái (hoặc nhấn phím tắt `Ctrl + O`).
3. Chọn thẻ **Files** và điều hướng chọn:
   * **Master Collection (Khuyến nghị)**:  
     👉 `docs/testing/postman/SP_ALL_GYM_Postman_Collection.json` (Gồm đầy đủ 260 API).
   * **File Environment**:  
     👉 `docs/testing/postman/GYM_Local.postman_environment.json` (Nếu chạy local).  
     👉 `docs/testing/postman/GYM_Server_DevOps.postman_environment.json` (Nếu chạy trên server AWS Staging).
4. Nhấn **Import**.

#### Bước 3: Kích hoạt Environment & Kiểm tra biến môi trường
1. Tại góc trên cùng bên phải cửa sổ Postman, nhấp vào menu thả xuống và chọn:  
   **`1. GYM Local Environment`** (hoặc `2. GYM Server DevOps (AWS Staging)`).
2. Nhấp biểu tượng con mắt (**Environment quick look**) để kiểm tra các biến đã được nạp:
   - `base_url`: `http://localhost:8080` (Mặc định local).
   - `admin_email`: `admin@gym.local` | `admin_password`: `123456`
   - `staff_email`: `staff@gym.local` | `staff_password`: `123456`
   - `token`, `admin_token`, `staff_token`: Sẽ tự động được điền khi chạy kịch bản Login.

> **💡 Cơ chế tự động hóa Token JWT (Auto-Auth):**  
> Trong tất cả các Collection, request Login đã tích hợp sẵn đoạn mã trong tab **Tests**:
> ```javascript
> if (pm.response.code === 200) {
>     var jsonData = pm.response.json();
>     var token = jsonData.token || (jsonData.data && jsonData.data.token);
>     pm.environment.set("admin_token", token);
>     pm.environment.set("token", token);
> }
> ```
> Người dùng không cần copy token thủ công. Toàn bộ các API phía sau sẽ tự động kế thừa header `Authorization: Bearer {{token}}`.

#### Bước 4: Chạy tự động bằng Postman Collection Runner
1. Nhấp chuột phải vào Collection: **`GYM Management System - Master API Test Suite (UC04 - UC14)`** $\rightarrow$ Chọn **Run collection**.
2. Tại cửa sổ **Runner**:
   - Chọn toàn bộ 12 thư mục (hoặc chọn phân hệ muốn kiểm thử).
   - Chọn Environment: **`1. GYM Local Environment`**.
   - Tick chọn: **Keep variable values** và **Save responses**.
   - Cài đặt Delay: `20 ms` (giúp database xử lý trơn tru).
3. Nhấp nút màu xanh/cam: **Run GYM Management System...**
4. Quan sát kết quả chạy: 100% assertions kiểm tra mã HTTP Status (200, 201, 400, 403, 404), schema JSON và logic nghiệp vụ sẽ hiển thị màu **Xanh lá (PASS)**.

---

### 4.2. Thực thi dòng lệnh qua Newman CLI & Xuất báo cáo HTML

Nếu muốn chạy tự động trên dòng lệnh Terminal hoặc tích hợp CI/CD:

```bash
# 1. Cài đặt Newman và công cụ xuất báo cáo HTML
npm install -g newman newman-reporter-htmlextra

# 2. Chạy toàn bộ Master Collection trên Terminal (Console Output)
newman run docs/testing/postman/SP_ALL_GYM_Postman_Collection.json \
  -e docs/testing/postman/GYM_Local.postman_environment.json \
  --delay-request 30

# 3. Chạy và tự động xuất Báo cáo HTML trực quan chuyên nghiệp
newman run docs/testing/postman/SP_ALL_GYM_Postman_Collection.json \
  -e docs/testing/postman/GYM_Local.postman_environment.json \
  -r cli,htmlextra \
  --reporter-htmlextra-export docs/testing/reports/Postman/Master_API_Report.html \
  --reporter-htmlextra-title "Báo Cáo Kiểm Thử Tự Động Toàn Văn API - PowerFit Gym"
```

Sau khi chạy xong, mở tệp `Master_API_Report.html` trực tiếp trên trình duyệt để xem Dashboard đồ họa trực quan gồm tỷ lệ Pass/Fail, thời gian phản hồi (Response Time) và chi tiết từng payload request/response.

---

## 5. HƯỚNG DẪN KIỂM THỬ GIAO DIỆN E2E BẰNG SELENIUM IDE TRÊN MICROSOFT EDGE

Kịch bản kiểm thử giao diện của dự án được đóng gói theo định dạng chuẩn quốc tế **Selenium IDE Project (`.side`)**. Trên trình duyệt **Microsoft Edge**, tiện ích mở rộng Selenium IDE cho phép mở giao diện trực quan, quan sát từng bước tự động click, điền form và bắt lỗi.

---

### 5.1. Cài đặt Selenium IDE Extension trên Microsoft Edge

Để cài đặt Selenium IDE vào Microsoft Edge, bạn thực hiện theo 1 trong 2 cách đơn giản sau:

#### Cách 1: Cài trực tiếp từ Chrome Web Store trên Edge (Khuyên dùng - Cập nhật mới nhất)
1. Mở trình duyệt **Microsoft Edge**.
2. Truy cập vào trang quản lý tiện ích: gõ `edge://extensions` vào thanh địa chỉ và nhấn Enter.
3. Bật công tắc: **"Cho phép tiện ích từ các cửa hàng khác" (Allow extensions from other stores)** ở góc dưới bên trái.
4. Truy cập liên kết: 👉 [Selenium IDE trên Chrome Web Store](https://chromewebstore.google.com/detail/selenium-ide/mooikfkahbdckldjjndioackbalphokd).
5. Nhấp nút **"Thêm vào Chrome" (Add to Chrome)** $\rightarrow$ Chọn **"Thêm phần mở rộng" (Add extension)**.

#### Cách 2: Cài từ Cửa hàng Tiện ích bổ sung của Microsoft Edge (Edge Add-ons)
1. Mở Microsoft Edge, truy cập: 👉 [Cửa hàng phần bổ trợ Edge (Edge Add-ons)](https://microsoftedge.microsoft.com/addons).
2. Tìm kiếm từ khóa: `Selenium IDE`.
3. Nhấp nút **Tải (Get)** để hoàn tất cài đặt.

> **📌 Mẹo ghim tiện ích:**  
> Sau khi cài đặt, nhấp vào biểu tượng mảnh ghép (Tiện ích) ở góc phải thanh địa chỉ của Microsoft Edge $\rightarrow$ Nhấp biểu tượng **con mắt (Show in toolbar)** cạnh Selenium IDE để ghim biểu tượng chữ **"Se"** màu ngọc lam ra ngoài thanh công cụ giúp mở nhanh.

---

### 5.2. Danh mục kịch bản Selenium IDE (.side) sẵn có

Tất cả các tệp kịch bản được lưu trữ trong thư mục [`docs/testing/selenium/`](selenium/):

| Tệp kịch bản (.side) | Phân hệ kiểm thử | Số ca test | Tài khoản kiểm thử |
| :--- | :--- | :---: | :--- |
| **`SP_ALL_GYM_Selenium_Suite.side`** | ⭐ **Toàn bộ 12 phân hệ chức năng hệ thống** | **222 TCs** | `admin@gym.local` / `staff@gym.local` |
| **`SP03.1_GYM_Public_UC01_to_UC04.side`** | Trang chủ Public, Đăng ký thẻ tập, Tra cứu, Đăng nhập | 15 TCs | Khách vãng lai / Member |
| **`SP03.2_GYM_Management_UC06_UC08_UC11.side`** | Quản lý Gói tập, Hội viên, Điểm danh check-in tại quầy | 24 TCs | `staff@gym.local` |
| **`SP03.3.1GYM_Management_UC12_Inventory.side`** | Quản lý Kho hàng, Danh mục sản phẩm, Phiếu nhập kho | 35 TCs | `admin@gym.local` |
| **`SP03.3.2GYM_Management_UC09_POS.side`** | Bán lẻ quầy POS, Giỏ hàng, Chặn vượt tồn, In hóa đơn | 31 TCs | `staff@gym.local` |
| **`SP03.4_GYM_Management_UC10_UC13_UC14_Admin.side`** | Admin: Quản lý Nhân sự, Voucher, Dashboard doanh thu | 55 TCs | `admin@gym.local` |
| **`SP03.4.1_GYM_Notification_UC03.side`** | Chuông thông báo Realtime Leads & Modal chi tiết | 27 TCs | `admin@gym.local` |
| **`SP03_UPDATE_GYM_Management_UC01_UC09_UC12.side`** | Bổ sung hiển thị ảnh sản phẩm POS & Form tư vấn KAN-285 | 35 TCs | `admin@gym.local` / `staff@gym.local` |

---

### 5.3. Các bước mở và chạy kịch bản trên Microsoft Edge

#### Bước 1: Khởi động hệ thống
Đảm bảo cả Backend và Frontend đang hoạt động bình thường trên máy tính:
* **Backend Spring Boot:** Đang chạy tại `http://localhost:8080`.
* **Frontend React Vite:** Đang chạy tại `http://localhost:5173` (chạy lệnh `npm run dev` trong thư mục `frontend/`).

#### Bước 2: Nạp tệp kịch bản `.side` vào Selenium IDE
1. Nhấp vào biểu tượng tiện ích **Selenium IDE** (chữ `Se` màu xanh) trên thanh công cụ của Microsoft Edge.
2. Cửa sổ làm việc của Selenium IDE hiện ra:
   - Nếu xuất hiện màn hình Welcome: Chọn dòng **`Open an existing project`** (hoặc `Load project`).
   - Nếu đang ở màn hình soạn thảo: Nhấp vào biểu tượng thư mục mở (**Open project**) ở góc trên bên trái.
3. Điều hướng tới thư mục dự án và chọn tệp:
   * **Chạy toàn bộ hệ thống (Khuyên dùng)**:  
     👉 `docs/testing/selenium/SP_ALL_GYM_Selenium_Suite.side`
   * *Hoặc chọn từng file phân hệ lẻ tùy nhu cầu kiểm thử*.
4. Bấm **Open**. Danh sách toàn bộ các Test Suites và Test Cases sẽ hiển thị đầy đủ ở khung điều hướng bên trái.

#### Bước 3: Cấu hình Playback Base URL
Tại thanh công cụ phía trên của cửa sổ Selenium IDE:
* Kiểm tra ô **Playback Base URL**, đảm bảo giá trị là:
  ```text
  http://localhost:5173
  ```
  *(Nếu đang để URL khác hoặc rỗng, hãy nhập chính xác `http://localhost:5173` để kịch bản trỏ vào Frontend local. Nếu muốn test trên môi trường AWS Staging, đổi thành URL AWS Staging tương ứng).*

#### Bước 4: Điều chỉnh tốc độ chạy (Test Execution Speed)
> **⚠️ Lưu ý quan trọng trên Microsoft Edge:**  
> Để tránh trình duyệt click quá nhanh khi DOM hoặc hiệu ứng animation chưa kịp render:
> 1. Nhấp vào biểu tượng **Đồng hồ đo tốc độ (Test execution speed)** ở thanh công cụ phía trên.
> 2. Kéo thanh trượt từ mức **Fast** lùi về mức **Middle (ở khoảng giữa)** — độ trễ khoảng 300ms–500ms giữa các bước.  
> Cài đặt này giúp các thông báo Toast và cửa sổ Modal hiển thị mượt mà, hạn chế tối đa tình trạng trượt phần tử do mạng chậm.

#### Bước 5: Thực thi kiểm thử
* **Chạy toàn bộ kịch bản trong 1 Suite (Khuyên dùng):**
  - Nhấp vào menu **Test Suites** ở góc trên danh sách bên trái.
  - Chọn suite mong muốn (ví dụ: `Master POS Suite` hoặc `Admin Suite`).
  - Nhấp vào biểu tượng **Run all tests in suite** (biểu tượng 2 hình tam giác `▶▶`).
* **Chạy 1 Test Case cụ thể:**
  - Nhấp chuột chọn ca test trong danh sách (ví dụ: `TC01_UI_01: Tải trang chủ`).
  - Nhấp vào biểu tượng **Run current test** (1 hình tam giác `▶`).

Microsoft Edge sẽ tự động mở tab mới, tự điền form, click nút, chọn dropdown và xác thực kết quả hoàn toàn tự động.

---

### 5.4. Đọc kết quả và phân tích Log kiểm thử

Trong và sau khi chạy kịch bản trên Microsoft Edge, bạn quan sát trực tiếp:
1. **Khung danh sách Test Cases (Bên trái):**
   * 🟢 **Màu xanh lá cây (Passed):** Ca kiểm thử thành công 100%, mọi bước assert giá trị, text, button đều khớp đặc tả.
   * 🔴 **Màu đỏ (Failed):** Phát hiện lỗi giao diện, sai lệch text hoặc không tìm thấy phần tử.
2. **Khung chi tiết các câu lệnh (Commands Table - Ở giữa):**
   * Từng câu lệnh (`open`, `type`, `click`, `assert text`, `waitForElementVisible`) chuyển màu xanh khi vượt qua.
3. **Tab Log (Khung dưới cùng):**
   * Ghi nhận nhật ký từng mili-giây: thời gian tải trang, giá trị nhập vào, kết quả so sánh thực tế vs kỳ vọng.
   * Nếu có lỗi, tab Log sẽ in đậm chi tiết vị trí bước lệnh và selector XPath/CSS để dễ dàng đối chiếu mã nguồn.

---

## 6. QUY TRÌNH CHUẨN BỊ MÔI TRƯỜNG & RESET CSDL 1-CHẠM

Để đảm bảo kết quả kiểm thử luôn đồng nhất và có thể tái hiện chính xác 100%, hệ thống cung cấp sẵn các script làm mới CSDL tự động:

### 6.1. Reset Cơ sở Dữ liệu 1-Chạm (< 3 giây)
Trước mỗi phiên kiểm thử lớn, hãy đưa CSDL về trạng thái chuẩn ban đầu (nạp sẵn tài khoản Admin, Staff, Hội viên mẫu và danh mục sản phẩm):

* **Trên hệ điều hành Windows:**
  ```cmd
  scripts\reset-test-db.bat
  ```
  *(Hoặc nhấp đúp trực tiếp vào tệp `scripts/reset-test-db.bat` trong thư mục dự án).*

* **Trên Linux / macOS:**
  ```bash
  ./scripts/reset-test-db.sh
  ```

### 6.2. Danh sách Tài khoản Kiểm thử Mặc định

| Tài khoản | Vai trò (Role) | Email đăng nhập | Mật khẩu | Phạm vi phân quyền |
| :--- | :--- | :--- | :--- | :--- |
| **Admin Quản trị** | `ROLE_ADMIN` | `admin@gym.local` | `123456` | Toàn quyền: Nhân sự, Voucher, Dashboard, Báo cáo |
| **Thu ngân (Staff)** | `ROLE_STAFF` | `staff@gym.local` | `123456` | Bán hàng POS, Quản lý hội viên, Điểm danh check-in |
| **Tài khoản bị khóa** | `ROLE_STAFF` | `staff.locked@gym.local` | `123456` | Kiểm thử chặn đăng nhập tài khoản vô hiệu hóa |

---

## 7. DANH MỤC BÁO CÁO MINH CHỨNG NGHIỆM THU

Toàn bộ kết quả thực thi kiểm thử thực tế đã được chụp ảnh màn hình phân giải cao, đính kèm kết quả assert và đóng gói thành các báo cáo chính thức trong thư mục `docs/testing/reports/`:

### Báo cáo Kiểm thử API Postman (`docs/testing/reports/Postman/`):
* 📘 `SP03.1_Minh_chung_Kiem_thu_API_Postman.docx`: Minh chứng API Trang chủ Public, Đăng ký gói tập & Tra cứu.
* 📘 `SP03.2_Minh_chung_Kiem_thu_API_Postman.docx`: Minh chứng API Quản lý Hội viên, Thẻ tập & Check-in quầy.
* 📘 `SP03.3.1_Minh_chung_Kiem_thu_API_Postman.docx`: Minh chứng API Kho hàng & Phiếu nhập kho.
* 📘 `SP03.3.2_Minh_chung_Kiem_thu_API_Postman.docx`: Minh chứng API Bán hàng POS & Rollback CSDL khi lỗi.
* 📘 `SP03.4_Minh_chung_Kiem_thu_API_Postman.docx`: Minh chứng API Nhân sự, Voucher & Báo cáo doanh thu.
* 📘 `SP03.4.1_Minh_chung_Kiem_thu_API_Postman.docx`: Minh chứng API Chuông thông báo Realtime Leads.
* 📘 `SP03_Update_Minh_Chung_Kiem_thu_Api_Postman.docx`: Minh chứng cập nhật API KAN-285 (Ảnh sản phẩm).

### Báo cáo Kiểm thử Giao diện Selenium UI (`docs/testing/reports/Selenium/`):
* 📗 `SP03.1_Minh_chung_Kiem_thu_Selenium_UI_Public.docx`: Minh chứng giao diện khách hàng vãng lai.
* 📗 `SP03.2_Minh_chung_Kiem_thu_Selenium_UI.docx`: Minh chứng giao diện quản lý hội viên & gói tập.
* 📗 `SP03.3.1_Minh_chung_Kiem_thu_Selenium_UI.docx`: Minh chứng giao diện quản lý kho hàng & nhập kho.
* 📗 `SP03.3.2_Minh_chung_Kiem_thu_Selenium_UI.docx`: Minh chứng giao diện giỏ hàng POS & in hóa đơn.
* 📗 `SP03.4_Minh_chung_Kiem_thu_Selenium_UI.docx`: Minh chứng giao diện quản trị Admin & Dashboard tài chính.
* 📗 `SP03.4.1_Minh_chung_Kiem_thu_Selenium_UI.docx`: Minh chứng giao diện chuông thông báo realtime.
* 📗 `SP03_Update_Minh_Chung_Kiem_thu_Selenium_UI.docx`: Minh chứng giao diện cập nhật ảnh sản phẩm POS.

### Báo cáo Tổng hợp Kiểm thử Toàn văn:
* 📕 `Test_Summary_Report_G04.docx` / `Test_Summary_Report_G04.pdf`: Báo cáo tổng kết toàn văn kiểm thử.
* 📊 [Testcases - Group-08.xlsx](file:///c:/Users/tn180/OneDrive/Desktop/GYM-management-develop/excel/Testcases%20-%20Group-08.xlsx): File Excel tổng hợp 482 Test cases và 7 Defect kèm ảnh nhúng.
* 📄 [CHƯƠNG_5_KIEM_THU_PHAN_MEM_GROUP_08.md](file:///c:/Users/tn180/OneDrive/Desktop/GYM-management-develop/sp05/CH%C6%AF%C6%A0NG_5_KIEM_THU_PHAN_MEM_GROUP_08.md): Toàn văn Chương 5 Báo cáo Đồ án tốt nghiệp / đồ án môn học.

---

## 8. KHẮC PHỤC SỰ CỐ THƯỜNG GẶP (TROUBLESHOOTING)

#### 1. Lỗi `ECONNREFUSED` khi chạy Postman hoặc không kết nối được cổng 8080
* **Nguyên nhân:** Máy chủ Backend Spring Boot chưa khởi động hoặc MySQL local bị tắt.
* **Khắc phục:**
  1. Kiểm tra dịch vụ MySQL đang chạy (port 3306).
  2. Mở terminal tại thư mục `backend/` và chạy: `mvn spring-boot:run`.
  3. Kiểm tra endpoint: `curl http://localhost:8080/api/public/packages`.

#### 2. Lỗi `401 Unauthorized` hoặc `Token Expired` trong Postman
* **Nguyên nhân:** Token JWT trong Environment đã quá hạn (thời hạn mặc định 24h) hoặc bị rỗng.
* **Khắc phục:** Chạy request `POST /api/auth/login` (Admin hoặc Staff) đầu tiên để script tự động gán token mới vào biến `{{token}}`.

#### 3. Lỗi Selenium trên Microsoft Edge báo `Element not found` hoặc `Timed out`
* **Nguyên nhân:** Trình duyệt Microsoft Edge xử lý tải trang hoặc render component React chậm hơn tốc độ click của Selenium.
* **Khắc phục:**
  1. Trong Selenium IDE, mở biểu tượng **Đồng hồ đo tốc độ** và kéo về mức **Middle (ở giữa)**.
  2. Đảm bảo tab Frontend `http://localhost:5173` đang mở và không bị Edge đưa vào trạng thái "Tab ngủ" (Sleeping Tabs). Có thể tắt tính năng Sleeping Tabs cho localhost tại `edge://settings/system`.

#### 4. Tiện ích Selenium IDE không hiển thị trên thanh công cụ Microsoft Edge
* **Khắc phục:** Nhấp vào biểu tượng **Mảnh ghép (Extensions)** ở góc phải thanh địa chỉ của Edge $\rightarrow$ Tìm `Selenium IDE` và bấm biểu tượng **Con mắt (Show in toolbar)**.

---

*Tài liệu được cập nhật và kiểm thử thành công vào ngày 10/11/2026 bởi Lead Tester Nguyễn Văn Thanh và Nhóm 08 (Lớp N04).*
