# 🧪 TÀI LIỆU HƯỚNG DẪN THIẾT LẬP VÀ CHẠY KIỂM THỬ HỆ THỐNG
> **Dự án:** PowerFit GYM Management System  
> **Phân hệ phụ trách:** Toàn bộ hệ thống (Public Website, POS Bán lẻ, Quản lý Kho, Quản lý Nhân sự, Voucher & Báo cáo Doanh thu)  
> **Dành cho:** Project Manager (PM), Hội đồng Đánh giá / Nghiệm thu, Developers và Đội ngũ Kiểm thử (QA/Tester)  
> **Phiên bản:** Sprint 3.4 (Hoàn thiện chặng 4 - Nghiệm thu tích hợp toàn diện)

---

## 📑 MỤC LỤC
1. [Tổng Quan Về Bộ Tài Nguyên Kiểm Thử](#1-tổng-quan-về-bộ-tài-nguyên-kiểm-thử)
2. [Cấu Trúc Thư Mục Chuẩn Mực `docs/testing/`](#2-cấu-trúc-thư-mục-chuẩn-mực-docstesting)
3. [Yêu Cầu Môi Trường & Công Cụ (Prerequisites)](#3-yêu-cầu-môi-trường--công-cụ-prerequisites)
4. [Hướng Dẫn Kiểm Thử API Bằng Postman & Newman CLI](#4-hướng-dẫn-kiểm-thử-api-bằng-postman--newman-cli)
   - [4.1. Thực thi trên ứng dụng Postman Desktop (Giao diện)](#41-thực-thi-trên-ứng-dụng-postman-desktop-giao-diện)
   - [4.2. Thực thi tự động không đầu qua Newman CLI (Dòng lệnh & Xuất báo cáo HTML)](#42-thực-thi-tự-động-không-đầu-qua-newman-cli-dòng-lệnh--xuất-báo-cáo-html)
5. [Hướng Dẫn Kiểm Thử Giao Diện E2E Bằng Selenium](#5-hướng-dẫn-kiểm-thử-giao-diện-e2e-bằng-selenium)
   - [5.1. Thực thi kịch bản qua Selenium IDE Extension](#51-thực-thi-kịch-bản-qua-selenium-ide-extension)
   - [5.2. Thực thi dòng lệnh qua Selenium Side Runner](#52-thực-thi-dòng-lệnh-qua-selenium-side-runner)
   - [5.3. Thực thi kịch bản bằng mã nguồn Python Selenium](#53-thực-thi-kịch-bản-bằng-mã-nguồn-python-selenium)
6. [Quy Trình Chuẩn Bị CSDL & Trích Xuất Log Đối Soát](#6-quy-trình-chuẩn-bị-csdl--trích-xuất-log-đối-soát)
7. [Danh Mục Báo Cáo Minh Chứng Nghiệm Thu](#7-danh-mục-báo-cáo-minh-chứng-nghiệm-thu)
8. [Khắc Phục Sự Cố Thường Gặp (Troubleshooting)](#8-khắc-phục-sự-cố-thường-gặp-troubleshooting)

---

## 1. TỔNG QUAN VỀ BỘ TÀI NGUYÊN KIỂM THỬ

Thư mục `docs/testing/` được chuẩn hóa nhằm cung cấp một **không gian lưu trữ tài nguyên kiểm thử tập trung, chuẩn mực và sẵn sàng tái hiện (reproducible)** trực tiếp trong repository của dự án. 

Tài liệu này cho phép:
- **Project Manager (PM) & Hội đồng nghiệm thu**: Dễ dàng import bộ sưu tập kịch bản kiểm thử, bấm chạy tự động 1-click hoặc qua 1 dòng lệnh CLI để kiểm chứng lại toàn bộ tính năng và báo cáo tỷ lệ PASS/FAIL thực tế.
- **Developers**: Chạy kiểm thử hồi quy (Regression Testing) ngay tại máy local trước khi tạo Pull Request để đảm bảo code mới không làm gãy các luồng nghiệp vụ hiện hữu.
- **QA / Testers**: Duy trì, bổ sung và quản lý phiên bản các kịch bản kiểm thử API (Postman) và giao diện E2E (Selenium).

---

## 2. CẤU TRÚC THƯ MỤC CHUẨN MỰC `docs/testing/`

Cây thư mục được phân định rõ ràng theo chức năng của từng loại tài nguyên:

```text
docs/testing/
├── README.md                                      # [Tài liệu này] Hướng dẫn thiết lập và chạy toàn bộ kịch bản test
├── TESTER_DB_RESET_AND_LOG_GUIDE.md               # Hướng dẫn reset CSDL siêu tốc & trích xuất log đối soát giao dịch
│
├── postman/                                       # Bộ sưu tập kịch bản kiểm thử API (Postman Collections & Environments)
│   ├── GYM_Local.postman_environment.json         # Biến môi trường chạy cục bộ (base_url: http://localhost:8080)
│   ├── GYM_Server_DevOps.postman_environment.json # Biến môi trường chạy server staging / production
│   ├── SP03.1_GYM_Postman_Collection.json         # Kịch bản API Sprint 3.1 (Public Web: Gói tập, Đăng ký, Checkin)
│   ├── SP03.2_GYM_Postman_Collection.json         # Kịch bản API Sprint 3.2 (Quản lý Hội viên, Thẻ tập, Lịch tập)
│   ├── SP03.3.1_GYM_Postman_Collection.json       # Kịch bản API Sprint 3.3.1 (Quản lý Kho hàng & Nhập kho)
│   ├── SP03.3.2_GYM_Postman_Collection.json       # Kịch bản API Sprint 3.3.2 (Bán hàng POS & Transaction Rollback)
│   └── SP03.4_GYM_Postman_Collection.json         # Kịch bản API Sprint 3.4 (Nhân sự UC10, Voucher UC13, Báo cáo UC14)
│
├── selenium/                                      # Bộ kịch bản kiểm thử giao diện tự động E2E (Selenium IDE & Script)
│   ├── SP03.1_GYM_Public_UC01_to_UC04.side        # Kịch bản UI Sprint 3.1 (Giao diện khách vãng lai, đăng ký, tra cứu)
│   ├── SP03.2_GYM_Management_UC06_UC08_UC11.side  # Kịch bản UI Sprint 3.2 (Giao diện quản lý gói tập, hội viên)
│   ├── SP03.3.1GYM_Management_UC12_Inventory.side # Kịch bản UI Sprint 3.3.1 (Giao diện Kho hàng, Danh mục, Nhập kho)
│   ├── SP03.3.2GYM_Management_UC09_POS.side       # Kịch bản UI Sprint 3.3.2 (Giao diện Bán hàng POS & In hóa đơn)
│   ├── SP03.4_GYM_Management_UC10_UC13_UC14_Admin.side # Kịch bản UI Sprint 3.4 (Quản trị Admin: Staff, Voucher, Báo cáo)
│   └── test_pos_uc09.py                           # Script Python tự động hóa Selenium E2E luồng POS
│
└── reports/                                       # Báo cáo Word (.docx) chứa minh chứng ảnh chụp thực thi chi tiết
    ├── Postman/                                   # Minh chứng kết quả kiểm thử API Postman
    │   ├── SP03.1_Minh_chung_Kiem_thu_API_Postman.docx
    │   ├── SP03.2_Minh_chung_Kiem_thu_API_Postman.docx
    │   ├── SP03.3.1_Minh_chung_Kiem_thu_API_Postman.docx
    │   ├── SP03.3.2_Minh_chung_Kiem_thu_API_Postman.docx
    │   └── SP03.4_Minh_chung_Kiem_thu_API_Postman.docx
    │
    └── Selenium/                                  # Minh chứng kết quả kiểm thử giao diện Selenium UI
        ├── SP03.1_Minh_chung_Kiem_thu_Selenium_UI_Public.docx
        ├── SP03.2_Minh_chung_Kiem_thu_Selenium_UI.docx
        ├── SP03.3.1_Minh_chung_Kiem_thu_Selenium_UI.docx
        ├── SP03.3.2_Minh_chung_Kiem_thu_Selenium_UI.docx
        └── SP03.4_Minh_chung_Kiem_thu_Selenium_UI.docx
```

---

## 3. YÊU CẦU MÔI TRƯỜNG & CÔNG CỤ (PREREQUISITES)

Để chạy trọn vẹn các kịch bản kiểm thử, máy tính cần đáp ứng các điều kiện tiên quyết sau:

| Công cụ / Nền tảng | Phiên bản khuyến nghị | Mục đích sử dụng | Liên kết cài đặt / Ghi chú |
| :--- | :--- | :--- | :--- |
| **Postman Desktop** | v10.x hoặc mới nhất | Chạy giao diện kiểm thử API Runner | [Tải Postman](https://www.postman.com/downloads/) |
| **Node.js & npm** | Node.js v18.x hoặc v20.x LTS | Cài đặt và chạy Newman CLI | [Tải Node.js](https://nodejs.org/) |
| **Newman CLI** | v5.3.x hoặc v6.x | Chạy kiểm thử API Postman qua Terminal | Cài qua npm: `npm install -g newman` |
| **newman-reporter-htmlextra** | v1.22.x trở lên | Xuất báo cáo HTML kiểm thử API trực quan | `npm install -g newman-reporter-htmlextra` |
| **Google Chrome Browser** | Bản mới nhất (v120+) | Trình duyệt thực thi kiểm thử giao diện | Trình duyệt Chrome chuẩn |
| **Selenium IDE Extension** | v3.17.x hoặc mới nhất | Mở và chạy trực quan các file `.side` | [Cài từ Chrome Web Store](https://chromewebstore.google.com/detail/selenium-ide/mooikfkahbdckldjjndioackbalphokd) |
| **Python** *(Tùy chọn)* | Python 3.10 trở lên | Chạy script tự động hóa Python Selenium | Đã cài gói `selenium`, `webdriver-manager` |
| **Java JDK** | OpenJDK 21 (Temurin / Oracle) | Khởi chạy máy chủ Backend Spring Boot | `java -version` hiển thị Java 21 |
| **MySQL Server** | MySQL 8.0.x (Port 3306/3307) | Cơ sở dữ liệu chính của hệ thống | Quản lý schema bằng Flyway migration |

---

## 4. HƯỚNG DẪN KIỂM THỬ API BẰNG POSTMAN & NEWMAN CLI

### 4.1. Thực thi trên ứng dụng Postman Desktop (Giao diện)

#### Bước 1: Khởi động hệ thống Backend
Đảm bảo Backend Spring Boot đã được khởi chạy và sẵn sàng nhận kết nối tại `http://localhost:8080`:
```bash
# Kiểm tra nhanh endpoint health/ping
curl http://localhost:8080/api/public/packages
```

#### Bước 2: Import Bộ sưu tập (Collection) & Biến môi trường (Environment)
1. Mở ứng dụng **Postman Desktop**.
2. Nhấp nút **Import** (ở góc trên cùng bên trái, hoặc nhấn phím tắt `Ctrl + O`).
3. Chọn thẻ **Files** hoặc kéo thả đồng thời:
   - File Collection: `docs/testing/postman/SP03.4_GYM_Postman_Collection.json` (hoặc các file `SP03.x` cần kiểm thử).
   - File Environment: `docs/testing/postman/GYM_Local.postman_environment.json`.
4. Nhấn **Import** để hoàn tất nạp kịch bản vào Workspace.

#### Bước 3: Kích hoạt Environment & Cấu hình biến môi trường
1. Tại góc trên cùng bên phải màn hình Postman, nhấp vào dropdown chọn môi trường và chọn **`1. GYM Local Environment`**.
2. Nhấp biểu tượng con mắt (Environment Quick Look) để kiểm tra các biến đã được nạp:
   - `base_url`: `http://localhost:8080` (Mặc định máy local)
   - `admin_email`: `admin@gym.local`
   - `admin_password`: `123456`
   - `staff_email`: `staff@gym.local`
   - `staff_password`: `123456`
   - `token`, `admin_token`, `staff_token`: Tự động được điền khi chạy kịch bản đăng nhập.

> **💡 Cơ chế tự động lấy Bearer Token:**  
> Trong các Collection (ví dụ: `SP03.4_GYM_Postman_Collection.json`), request Login đã được tích hợp sẵn script trong mục **Tests**:
> ```javascript
> if (pm.response.code === 200) {
>     var jsonData = pm.response.json();
>     var token = jsonData.token || (jsonData.data && jsonData.data.token);
>     pm.environment.set("admin_token", token);
>     pm.environment.set("token", token);
> }
> ```
> Do đó, người kiểm thử không cần copy token thủ công. Toàn bộ các request phía sau sẽ tự động kế thừa `Authorization: Bearer {{token}}`.

#### Bước 4: Chạy toàn bộ kịch bản bằng Postman Collection Runner
1. Nhấp chuột phải vào tên Collection (ví dụ: **`SP03.4 - Admin Management (UC10, UC13, UC14)`**) -> Chọn **Run collection**.
2. Cửa sổ Runner mở ra:
   - Chọn tất cả các folder hoặc các Test Cases mong muốn.
   - Chọn Environment: **`1. GYM Local Environment`**.
   - Tick chọn mục: **Keep variable values** và **Save responses**.
3. Nhấp nút màu cam: **Run SP03.4 - Admin Management...**
4. Quan sát kết quả chạy thời gian thực: Toàn bộ assertions kiểm tra mã HTTP Status (200, 201, 400, 403, 409), cấu trúc JSON Schema và thông điệp lỗi sẽ hiển thị màu **Xanh (Pass)**.

---

### 4.2. Thực thi tự động không đầu qua Newman CLI (Dòng lệnh & Xuất báo cáo HTML)

Newman cho phép chạy toàn bộ kịch bản kiểm thử API trực tiếp từ Command Prompt, PowerShell hoặc Terminal mà không cần mở ứng dụng Postman.

#### Cài đặt Newman và công cụ tạo báo cáo HTML:
```bash
npm install -g newman newman-reporter-htmlextra
```

#### Lệnh 1: Chạy kiểm thử API hiển thị trực tiếp trên Terminal (Console Output)
```bash
# Chạy kịch bản Sprint 3.4 (UC10, UC13, UC14)
newman run docs/testing/postman/SP03.4_GYM_Postman_Collection.json \
  -e docs/testing/postman/GYM_Local.postman_environment.json \
  --delay-request 50
```

#### Lệnh 2: Chạy kiểm thử và tự động xuất Báo cáo HTML chuyên nghiệp (`htmlextra`)
```bash
# Lệnh Windows PowerShell / CMD
newman run docs/testing/postman/SP03.4_GYM_Postman_Collection.json `
  -e docs/testing/postman/GYM_Local.postman_environment.json `
  -r cli,htmlextra `
  --reporter-htmlextra-export docs/testing/reports/Postman/Report_SP03.4_Newman.html `
  --reporter-htmlextra-title "Báo Cáo Kiểm Thử Tự Động API - Sprint 3.4 PowerFit Gym" `
  --reporter-htmlextra-browserTitle "API Test Report SP03.4"
```

Sau khi chạy xong, mở tệp `docs/testing/reports/Postman/Report_SP03.4_Newman.html` trên trình duyệt để xem dashboard trực quan gồm biểu đồ tròn số ca Pass/Fail, thời gian phản hồi (response time), kích thước payload và chi tiết từng request.

#### Lệnh 3: Chạy nhanh kịch bản các phân hệ khác:
```bash
# Phân hệ Bán hàng POS & Transaction Rollback (UC09)
newman run docs/testing/postman/SP03.3.2_GYM_Postman_Collection.json -e docs/testing/postman/GYM_Local.postman_environment.json

# Phân hệ Quản lý Kho hàng & Nhập kho (UC12)
newman run docs/testing/postman/SP03.3.1_GYM_Postman_Collection.json -e docs/testing/postman/GYM_Local.postman_environment.json

# Phân hệ Quản lý Hội viên & Thẻ tập (UC06, UC08, UC11)
newman run docs/testing/postman/SP03.2_GYM_Postman_Collection.json -e docs/testing/postman/GYM_Local.postman_environment.json
```

---

## 5. HƯỚNG DẪN KIỂM THỬ GIAO DIỆN E2E BẰNG SELENIUM IDE

> **⭐ Chuẩn kiểm thử giao diện của dự án:**  
> Toàn bộ kịch bản kiểm thử giao diện (UI E2E) từ **SP03.1 đến SP03.4** được xây dựng và đóng gói chuẩn hóa dưới định dạng **Selenium IDE Project (`.side`)**.  
> Phương thức thực thi chính thức và khuyến nghị là sử dụng **tiện ích mở rộng Selenium IDE (Browser Extension)** trên trình duyệt Chrome, Edge hoặc Firefox, giúp quan sát trực quan từng thao tác click, nhập liệu, mở modal và xác thực Assertion thời gian thực.

---

### 5.1. Danh Sách Các Tệp Kịch Bản Selenium IDE (`.side`)
Tất cả các tệp kịch bản được lưu trữ sẵn trong thư mục [`docs/testing/selenium/`](selenium/):

| Tệp kịch bản (.side) | Phân hệ kiểm thử | Số ca kiểm thử (TCs) | Tài khoản sử dụng |
| :--- | :--- | :--- | :--- |
| **`SP03.1_GYM_Public_UC01_to_UC04.side`** | Trang chủ Public, Đăng ký thẻ tập, Tra cứu thông tin, Gửi liên hệ | 15 TCs | Khách vãng lai |
| **`SP03.2_GYM_Management_UC06_UC08_UC11.side`** | Quản lý Gói tập, Đăng ký Hội viên, Check-in tại quầy | 24 TCs | `staff@gym.local` / `admin@gym.local` |
| **`SP03.3.1GYM_Management_UC12_Inventory.side`** | Quản lý Kho hàng, Danh mục sản phẩm, Tạo phiếu nhập kho | 35 TCs | `admin@gym.local` / `staff@gym.local` |
| **`SP03.3.2GYM_Management_UC09_POS.side`** | Bán hàng POS tại quầy, Giỏ hàng, Chặn vượt tồn, In hóa đơn | 31 TCs | `staff@gym.local` |
| **`SP03.4_GYM_Management_UC10_UC13_UC14_Admin.side`** | Quản trị Admin: Quản lý Nhân sự (UC10), Voucher (UC13), Dashboard & Báo cáo Doanh thu (UC14) | 55 TCs | `admin@gym.local` |

---

### 5.2. Hướng Dẫn Cài Đặt Selenium IDE Extension
Nếu trình duyệt của bạn chưa có tiện ích Selenium IDE:
1. Mở trình duyệt Google Chrome (hoặc Microsoft Edge).
2. Truy cập https://www.selenium.dev/selenium-ide/.
3. Chọn "Latest zip" để chuyển sang github, chọn file có đuôi .exe, tải về.
4. Cài đặt.


---

### 5.3. Các Bước Mở & Chạy Kịch Bản Kiểm Thử Bằng Selenium IDE

#### Bước 1: Khởi động hệ thống
Đảm bảo cả Backend và Frontend của dự án đang chạy ổn định:
- **Backend Spring Boot:** Đang chạy tại `http://localhost:8080`.
- **Frontend React:** Đang chạy tại `http://localhost:5173` (chạy lệnh `npm run dev` trong thư mục `frontend/`).

#### Bước 2: Nạp (Open) tệp kịch bản `.side` vào Selenium IDE
1. Nhấp vào biểu tượng **Selenium IDE**.
2. Tại màn hình khởi động (Welcome screen), chọn dòng:
   👉 **`Load project`**
3. Cửa sổ chọn tệp xuất hiện, điều hướng đến thư mục dự án và chọn tệp `.side` mong muốn:
   - Đường dẫn: `docs/testing/selenium/SP03.4_GYM_Management_UC10_UC13_UC14_Admin.side` *(hoặc các phân hệ khác)*.
4. Bấm **Open**. Cửa sổ Selenium IDE sẽ hiển thị đầy đủ danh sách các Test Suites và Test Cases ở khung điều hướng bên trái.

#### Bước 3: Cấu hình Playback Base URL
Tại thanh công cụ phía trên của cửa sổ Selenium IDE:
- Kiểm tra ô **Playback Base URL**, đảm bảo giá trị là:
  ```text
  http://localhost:5173
  ```
  *(Nếu đang để URL khác hoặc rỗng, hãy nhập chính xác `http://localhost:5173` để kịch bản trỏ đúng vào giao diện Frontend).*

#### Bước 4: Điều chỉnh tốc độ chạy (Test Execution Speed)
> **💡 Mẹo tối ưu:**  
> Để tránh trình duyệt click nhanh hơn tốc độ render DOM hoặc tốc độ phản hồi API:
> - Nhấp vào biểu tượng **Đồng hồ đo tốc độ (Test execution speed)** ở góc trên thanh công cụ.
> - Kéo thanh trượt từ mức **Fast** lùi về khoảng giữa (**Middle** - khoảng 500ms giữa các bước).  
> Điều này giúp giao diện hiển thị mượt mà, Toast message và Modal kịp bật lên trước khi bước tiếp theo kiểm tra.

#### Bước 5: Bắt đầu thực thi kiểm thử
* **Chạy 1 Test Case cụ thể:**
  - Nhấp chuột chọn 1 test case trong danh sách bên trái (ví dụ: `TC75_UI_75: Tải trang Quản lý Nhân sự`).
  - Nhấp vào biểu tượng **Run current test** (1 hình tam giác `▶`).

---

### 5.4. Đọc & Đánh Giá Kết Quả Kiểm Thử Trong Selenium IDE

Trong suốt quá trình chạy, bạn có thể quan sát trực tiếp:
1. **Tại khung danh sách kịch bản (Test Cases List):**
   - 🟢 **Màu xanh lá cây (Passed):** Ca kiểm thử đạt 100%, mọi bước click, verify text, assert value đều khớp đặc tả.
   - 🔴 **Màu đỏ (Failed):** Ca kiểm thử thất bại hoặc phát hiện bug giao diện.
2. **Tại khung chi tiết các bước lệnh (Commands Table):**
   - Từng câu lệnh (`open`, `type`, `click`, `assert text`, `waitForElementPresent`) sẽ được tô màu xanh khi thực thi thành công.
3. **Tại tab Log (Khung dưới cùng):**
   - Hiển thị nhật ký chi tiết từng mili-giây: thời gian tải trang, giá trị nhập vào, nội dung so sánh thực tế vs kỳ vọng.
   - Nếu gặp lỗi, tab Log sẽ chỉ rõ tên lệnh và selector thất bại để Tester/Dev dễ dàng đối chiếu.

---

### 5.5. Tùy Chọn Bổ Sung: Chạy Dòng Lệnh Bằng Selenium Side Runner (CLI)
Dành cho trường hợp muốn tích hợp chạy tự động trong CI/CD hoặc chạy nền từ Terminal:
```bash
# 1. Cài đặt runner
npm install -g selenium-side-runner chromedriver

# 2. Chạy tệp .side trực tiếp qua dòng lệnh
selenium-side-runner docs/testing/selenium/SP03.4_GYM_Management_UC10_UC13_UC14_Admin.side --base-url http://localhost:5173
```

---

## 6. QUY TRÌNH CHUẨN BỊ CSDL & TRÍCH XUẤT LOG ĐỐI SOÁT

Để đảm bảo kết quả kiểm thử luôn đồng nhất và có thể tái hiện chính xác 100%, hệ thống cung cấp sẵn các script tự động hóa:

### 6.1. Reset Cơ sở Dữ liệu 1-Chạm (< 3 giây)
Trước khi chạy kịch bản kiểm thử, nên làm mới CSDL về trạng thái chuẩn (nạp đầy đủ 8 migration Flyway V1-V4, V900-V904 và tài khoản mẫu):

* **Trên Windows:**
  ```cmd
  scripts\reset-test-db.bat
  ```
  *(Hoặc nhấp đúp trực tiếp vào file `scripts/reset-test-db.bat` trong thư mục dự án).*

* **Trên Linux / macOS:**
  ```bash
  ./scripts/reset-test-db.sh
  ```

### 6.2. Danh sách Tài khoản & Dữ liệu Kiểm thử Sẵn có

| Tài khoản | Vai trò (Role) | Email đăng nhập | Mật khẩu | Phạm vi phân quyền |
| :--- | :--- | :--- | :--- | :--- |
| **Admin** | `ROLE_ADMIN` | `admin@gym.local` | `123456` | Toàn quyền: Nhân sự, Voucher, Dashboard, Báo cáo |
| **Staff (Thu ngân)** | `ROLE_STAFF` | `staff@gym.local` | `123456` | Bán hàng POS, Quản lý hội viên, Check-in |
| **Staff Locked** | `ROLE_STAFF` | `staff.locked@gym.local` | `123456` | Tài khoản bị khóa (Dùng test chặn đăng nhập) |
| **Staff Inactive** | `ROLE_STAFF` | `staff.inactive@gym.local` | `123456` | Tài khoản ngừng hoạt động (Dùng test chặn đăng nhập) |

### 6.3. Trích xuất Log Đối soát Giao dịch & Rollback
Để lấy bằng chứng rollback đơn hàng hoặc đối soát CSDL:
```cmd
scripts\extract-transaction-logs.bat
```
*(Chi tiết xem thêm tại tài liệu: [`docs/testing/TESTER_DB_RESET_AND_LOG_GUIDE.md`](TESTER_DB_RESET_AND_LOG_GUIDE.md)).*

---

## 7. DANH MỤC BÁO CÁO MINH CHỨNG NGHIỆM THU

Toàn bộ kết quả chạy thực tế đã được chụp ảnh màn hình phân giải cao và đóng gói thành các báo cáo nghiệm thu chính thức tại thư mục `docs/testing/reports/`:

### Báo cáo API Postman (`docs/testing/reports/Postman/`):
- 📘 `SP03.1_Minh_chung_Kiem_thu_API_Postman.docx`: Minh chứng API Trang chủ Public, Đăng ký & Gói tập.
- 📘 `SP03.2_Minh_chung_Kiem_thu_API_Postman.docx`: Minh chứng API Quản lý Hội viên & Thẻ tập.
- 📘 `SP03.3.1_Minh_chung_Kiem_thu_API_Postman.docx`: Minh chứng API Kho hàng & Phiếu nhập kho.
- 📘 `SP03.3.2_Minh_chung_Kiem_thu_API_Postman.docx`: Minh chứng API Bán hàng POS & Transaction Rollback.
- 📘 `SP03.4_Minh_chung_Kiem_thu_API_Postman.docx`: Minh chứng 52 ca kiểm thử API Quản trị Nhân sự, Voucher & Báo cáo Doanh thu (100% PASS).

### Báo cáo Giao diện Selenium UI (`docs/testing/reports/Selenium/`):
- 📗 `SP03.1_Minh_chung_Kiem_thu_Selenium_UI_Public.docx`: Minh chứng giao diện khách hàng tra cứu & đăng ký.
- 📗 `SP03.2_Minh_chung_Kiem_thu_Selenium_UI.docx`: Minh chứng giao diện quản lý hội viên & gói tập.
- 📗 `SP03.3.1_Minh_chung_Kiem_thu_Selenium_UI.docx`: Minh chứng giao diện quản lý kho hàng & cảnh báo tồn kho.
- 📗 `SP03.3.2_Minh_chung_Kiem_thu_Selenium_UI.docx`: Minh chứng giao diện giỏ hàng POS, thanh toán & in hóa đơn.
- 📗 `SP03.4_Minh_chung_Kiem_thu_Selenium_UI.docx`: Minh chứng 55 ca kiểm thử giao diện Quản trị Admin, biểu đồ báo cáo và hồ sơ ghi nhận 2 lỗi UI.

---

## 8. KHẮC PHỤC SỰ CỐ THƯỜNG GẶP (TROUBLESHOOTING)

#### 1. Lỗi `ECONNREFUSED` hoặc không thể kết nối tới Backend (8080)
- **Nguyên nhân:** Máy chủ Backend Spring Boot chưa khởi động hoặc gặp lỗi kết nối cơ sở dữ liệu.
- **Cách khắc phục:**
  - Kiểm tra service MySQL đang chạy (port 3306 hoặc 3307).
  - Chạy lại lệnh reset CSDL: `scripts\reset-test-db.bat`.
  - Khởi động lại Backend: `cd backend && mvn spring-boot:run`.

#### 2. Lỗi `401 Unauthorized` hoặc `Token Expired` khi chạy Postman
- **Nguyên nhân:** Biến `token` trong Postman Environment bị rỗng hoặc đã hết hạn JWT (thời hạn mặc định 24h).
- **Cách khắc phục:** Trong Postman Runner, luôn đảm bảo request **Login (Admin/Staff)** được đặt ở đầu danh sách chạy để tự động cấp mới token vào biến môi trường trước khi thực hiện các API nghiệp vụ.

#### 3. Lỗi Selenium báo `Element not found` hoặc `Timed out waiting for element`
- **Nguyên nhân:** Giao diện Frontend React tải dữ liệu từ API chậm hơn tốc độ click của Selenium.
- **Cách khắc phục:**
  - Trong Selenium IDE, kéo thanh gạt **Test Execution Speed** (biểu tượng đồng hồ ở thanh trên cùng) từ mức *Fast* về mức *Middle* để thêm thời gian chờ ổn định DOM giữa các bước.
  - Đảm bảo Frontend đã build hoặc đang chạy chế độ dev ổn định (`npm run dev`).

---

**© 2026 PowerFit GYM Management - QA & Testing Team. All Rights Reserved.**

