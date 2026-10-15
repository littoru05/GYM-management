# ĐẶC TẢ YÊU CẦU PHẦN MỀM (SRS) - HỆ THỐNG QUẢN LÝ PHÒNG GYM

> **Dự án:** XÂY DỰNG WEBSITE QUẢN LÝ PHÒNG TẬP GYM  
> **Học phần:** Project 1 (N04 - Nhóm 08)  
> **Giảng viên hướng dẫn:** TS. Nguyễn Trọng Phúc  
> **Trường:** Đại học Giao thông Vận tải - Khoa Công nghệ Thông tin  

---

## MỤC LỤC TỔNG QUAN

- [1. Danh sách thành viên nhóm & Phân công nhiệm vụ](#bảng-phân-công-và-đánh-giá)
- [2. Chương 1: Tổng quan hệ thống](#chương-1-tổng-quan-hệ-thống)
- [3. Chương 2: Kiến trúc hệ thống & Công nghệ áp dụng](#chương-2-kiến-trúc-hệ-thống--công-nghệ-áp-dụng)
- [4. Chương 3: Phân tích & Đặc tả yêu cầu nghiệp vụ](#chương-3-phân-tích--đặc-tả-yêu-cầu-nghiệp-vụ)
  - [Danh mục các Ca sử dụng (Use Case Catalog)](#danh-mục-các-ca-sử-dụng-use-case-catalog)
  - [Sơ đồ Use Case tổng quan](#sơ-đồ-use-case-tổng-quan)
  - [Đặc tả chi tiết 14 Use Cases:](#đặc-tả-chi-tiết-use-case-kèm-activity-diagram--ui-wireframe)
    - [UC01: Xem trang chủ & dịch vụ](#xem-trang-chủ--dịch-vụ)
    - [UC02: Xem bảng giá & thông tin gói tập](#xem-bảng-giá--xem-thông-tin-gói-tập)
    - [UC03: Liên hệ tư vấn nhanh](#liên-hệ-tư-vấn-nhanh)
    - [UC04: Đăng nhập](#đăng-nhập)
    - [UC05: Đăng xuất](#đăng-xuất)
    - [UC06: Quản lý hội viên](#quản-lý-hội-viên)
    - [UC07: Bán gói tập (Membership)](#bán-gói-tập-membership)
    - [UC08: Điểm danh hội viên (Check-in)](#điểm-danh-hội-viên-check-in)
    - [UC09: Bán hàng bán lẻ (Mini POS)](#bán-hàng-bán-lẻ-mini-pos)
    - [UC10: Quản lý nhân viên](#quản-lý-nhân-viên)
    - [UC11: Quản lý gói tập](#quản-lý-gói-tập)
    - [UC12: Quản lý kho & hàng hóa](#quản-lý-kho--hàng-hóa)
    - [UC13: Quản lý khuyến mãi](#quản-lý-khuyến-mãi)
    - [UC14: Thống kê doanh thu](#thống-kê-doanh-thu)
- [5. Yêu cầu phi chức năng (NFR)](#yêu-cầu-phi-chức-năng-non-functional-requirements)

---

![Hình ảnh](images_srs/image1.jpeg)

#### Giảng viên hướng dẫn:	Sinh viên thực hiện: TS. Nguyễn Trọng Phúc

LỜI CẢM ƠN

Nhóm chúng em xin gửi lời cảm ơn chân thành và sâu sắc nhất đến thầy TS. Nguyễn Trọng Phúc, giảng viên hướng dẫn học phần Project 1, người đã tận tình chỉ bảo, định hướng và hỗ trợ chúng em trong quá trình thực hiện đề tài “Xây dựng website quản lý phòng tập GYM”.

Chúng em xin kính chúc thầy có thật nhiều sức khỏe và thành công trên con đường giảng dạy của mình.

Nhóm xin chân thành cảm ơn thầy!

Nhóm sinh viên thực hiện: (Nguyễn Ngọc Anh Linh – Nguyễn Công Hậu – Lê Xuân Khánh – Vũ Việt Tân – Nguyễn Văn Thanh)

# BẢNG PHÂN CÔNG VÀ ĐÁNH GIÁ

| STT | Họ và tên | Mã sinh viên | Vị trí | Công việc |
| --- | --- | --- | --- | --- |
| 1 | Nguyễn Ngọc Anh Linh | 231230823 | Project Manager | Lập kế hoạch, phân chia task và giám sát tiến độ dự án.<br>Điều phối hoạt động nhóm và quản lý rủi ro phát sinh.<br>Tổng hợp và rà soát tài liệu báo cáo. |
| 2 | Nguyễn Văn Thanh | 231230903 | Tester | Thiết kế kịch bản và bộ Test Cases từ Use Case.<br>Thực hiện kiểm thử chức năng, tích hợp và giao diện.<br>Ghi nhận, phân loại bug và viết báo cáo kiểm thử. |
| 3 | Lê Xuân Khánh | 231230809 | Frontend Developer | Thiết kế giao diện Wireframe / UI/UX hệ thống.<br>Phát triển giao diện người dùng và xử lý responsive.<br>Tích hợp API và phối hợp hoàn thiện trải nghiệm người dùng. |
| 4 | Vũ Việt<br>Tân | 231230897 | Backend Developer | Phân tích và thiết kế mô hình cơ sở dữ liệu.<br>Xây dựng và tối ưu các RESTful API cho hệ thống.<br>Xử lý logic nghiệp vụ và bảo mật phía máy chủ. |
| 5 | Nguyễn Công Hậu | 231230767 | DevOps | Quản lý môi trường phát triển và triển khai.<br>Giám sát và tối ưu hệ thống.<br>Tích hợp và triển khai liên tục.<br>Hỗ trợ nhóm phát triển. |

# DANH MỤC BẢNG BIỂU

*Bảng 3.1. Bảng xác định các ca sử dụng	15*

*Bảng 3.2. Đặc tả UC01: Xem trang chủ & dịch vụ	18*

*Bảng 3.3. Đặc tả UC02: Xem bảng giá & thông tin gói tập	21*

*Bảng 3.4. Đặc tả UC03: Liên hệ tư vấn nhanh	24*

*Bảng 3.5. Đặc tả UC04: Đăng nhập	27*

*Bảng 3.6. Đặc tả UC05: Đăng xuất	30*

*Bảng 3.7. Đặc tả UC06: Quản lý hội viên	34*

*Bảng 3.8. Đặc tả UC07: Bán gói tập	37*

*Bảng 3.9. Đặc tả UC08: Điểm danh hội viên	39*

*Bảng 3.10. Đặc tả UC09: Bán hàng bán lẻ (Mini POS)	42*

*Bảng 3.11. Đặc tả UC10: Quản lý nhân viên	46*

*Bảng 3.12. Đặc tả UC11: Quản lý gói tập	49*

*Bảng 3.13. Đặc tả UC12: Quản lý kho & hàng hóa	52*

*Bảng 3.14. Đặc tả UC13: Quản lý khuyến mãi	55*

*Bảng 3.15. Đặc tả UC14: Thống kê doanh thu	59*

# DANH MỤC HÌNH ẢNH

*Hình 3.1. Biểu đồ Use Case tổng quan	16*

*Hình 3.2. Biểu đồ hoạt động của UC01: Xem trang chủ & dịch vụ	17*

*Hình 3.2.1. Giao diện UI Demo của UC01: Xem trang chủ & dịch vụ	19*

*Hình 3.3. Biểu đồ hoạt động của UC02: Xem bảng giá & thông tin gói tập	20*

*Hình 3.3.1. Giao diện UI Demo của UC02: Xem bảng giá & thông tin gói tập	22*

*Hình 3.4. Biểu đồ hoạt động của UC03: Liên hệ tư vấn nhanh	23*

*Hình 3.4.1. Giao diện UI Demo của UC03: Liên hệ tư vấn nhanh	25*

*Hình 3.5. Biểu đồ hoạt động của UC04: Đăng nhập	26*

*Hình 3.5.1. Giao diện UI Demo của UC04: Đăng nhập	28*

*Hình 3.6. Biểu đồ hoạt động của UC05: Đăng xuất	29*

*Hình 3.6.1. Giao diện UI Demo của UC05: Đăng xuất	31*

*Hình 3.7. Biểu đồ hoạt động của UC06: Quản lý hội viên	32*

*Hình 3.7.1. Giao diện UI Demo của UC06: Quản lý hội viên	34*

*Hình 3.8. Biểu đồ hoạt động của UC07: Bán gói tập	35*

*Hình 3.8.1. Giao diện UI Demo của UC07: Bán gói tập	37*

*Hình 3.9. Biểu đồ hoạt động của UC08: Điểm danh hội viên	38*

*Hình 3.9.1. Giao diện UI Demo của UC08: Điểm danh hội viên	40*

*Hình 3.10. Biểu đồ hoạt động của UC09: Bán hàng bán lẻ (Mini POS)	41*

*Hình 3.10.1. Giao diện UI Demo của UC09: Bán hàng bán lẻ (Mini POS)	43*

*Hình 3.11. Biểu đồ hoạt động của UC10: Quản lý nhân viên	44*

*Hình 3.11.1. Giao diện UI Demo của UC10: Quản lý nhân viên	46*

*Hình 3.12. Biểu đồ hoạt động của UC11: Quản lý gói tập	47*

*Hình 3.12.1. Giao diện UI Demo của UC11: Quản lý gói tập	49*

*Hình 3.13. Biểu đồ hoạt động của UC12: Quản lý kho & hàng hóa	50*

*Hình 3.13.1. Giao diện UI Demo của UC12: Quản lý kho & hàng hóa	53*

*Hình 3.14. Biểu đồ hoạt động của UC13: Quản lý khuyến mãi	54*

*Hình 3.14.1. Giao diện UI Demo của UC13: Quản lý khuyến mãi	56*

*Hình 3.15. Biểu đồ hoạt động của UC14: Thống kê doanh thu	57*

*Hình 3.15.1. Giao diện UI Demo của UC14: Thống kê doanh thu	59*

# LỜI MỞ ĐẦU

Cùng với xu hướng số hóa quy trình quản lý hiện nay, việc ứng dụng công nghệ thông tin vào công tác vận hành các cơ sở dịch vụ, đặc biệt là các trung tâm thể hình (Gym & Fitness), đóng vai trò quan trọng trong việc nâng cao hiệu suất làm việc và chất lượng phục vụ khách hàng. Xuất phát từ nhu cầu thực tế đó, đề tài “Xây dựng website Quản lý phòng tập Gym” được thực hiện trong khuôn khổ học phần Project 1. Dự án tập trung giải quyết các bài toán nghiệp vụ cốt lõi tại quầy như kiểm soát thông tin hội viên, điểm danh thẻ tập, thanh toán bán lẻ (POS) và hỗ trợ quản trị viên theo dõi biến động doanh thu một cách chính xác, minh bạch.

Để hiện thực hóa mục tiêu đề ra, nhóm xây dựng kế hoạch và tổ chức làm việc theo quy trình phát triển phần mềm từng bước rõ ràng, kết hợp quản lý và theo dõi tiến độ công việc thông qua bảng Kanban trực quan. Cách tiếp cận này giúp các thành viên phân định trách nhiệm cụ thể, chủ động kiểm soát trạng thái các đầu việc và duy trì sự phối hợp chặt chẽ xuyên suốt dự án. Các thành viên trong nhóm lần lượt đảm nhiệm các vai trò chuyên môn từ quản lý dự án, thiết kế giao diện (UI/UX), phát triển hệ thống (Frontend/Backend), kiểm thử chất lượng cho đến nghiên cứu giải pháp vận hành. Quá trình thực hiện đề tài là cơ hội quý báu để nhóm củng cố kiến thức chuyên ngành, rèn luyện kỹ năng làm việc nhóm và giải quyết bài toán nghiệp vụ trong môi trường thực tế.

Nội dung báo cáo tập trung làm rõ việc khảo sát và phân tích yêu cầu nghiệp vụ, định hình mô hình kiến trúc phần mềm, lựa chọn nền tảng công nghệ phù hợp và chi tiết hóa các ca sử dụng của hệ thống. Nhóm hướng tới việc hoàn thiện một sản phẩm có tính ứng dụng cao, đáp ứng đầy đủ mục tiêu đào tạo và các tiêu chí đánh giá của học phần.

# CHƯƠNG 1: TỔNG QUAN HỆ THỐNG

## Bối cảnh & Lý do chọn đề tài:

Cùng với sự phát triển của đời sống kinh tế – xã hội, nhu cầu rèn luyện thể chất và chăm sóc sức khỏe thông qua các phòng tập thể hình (GYM & Fitness) ngày càng tăng cao. Tuy nhiên, phần lớn các phòng gym quy mô vừa và nhỏ hiện nay vẫn duy trì phương thức quản lý thủ công thông qua sổ sách truyền thống hoặc các bảng tính Excel rời rạc. Cách tiếp cận này bộc lộ nhiều hạn chế nghiêm trọng trong quá trình vận hành thực tế:

Thất thoát doanh thu và khó kiểm soát dòng tiền: Việc tính toán doanh thu gói tập kết hợp với các dịch vụ bán lẻ (nước giải khát, thực phẩm bổ sung, phụ kiện thể thao) dễ xảy ra sai lệch do không có cơ chế đối soát tức thì giữa hóa đơn và lượng hàng tồn kho.

Nhầm lẫn thời hạn và quyền lợi thẻ hội viên: Khâu theo dõi ngày kích hoạt, ngày hết hạn, số buổi tập còn lại hay lịch sử đóng băng/gia hạn thẻ thường xuyên gặp trục trặc, dẫn đến tình trạng tranh chấp quyền lợi và gây ấn tượng không tốt cho khách hàng.

Quy trình điểm danh chậm chạp: Việc tra cứu thông tin hội viên thủ công tại quầy lễ tân vào các khung giờ cao điểm gây ùn ứ, mất thời gian và thiếu tính chính xác.

Thiếu hụt dữ liệu báo cáo đa chiều: Ban quản trị không thể nắm bắt nhanh số liệu hội viên mới, tỷ lệ gia hạn thẻ hay sản phẩm bán chạy theo thời gian thực để đưa ra các chiến lược kinh doanh kịp thời.

Xuất phát từ những bất cập trên, việc phát triển một hệ thống quản lý phòng gym tích hợp ứng dụng quầy (POS) và website giới thiệu dịch vụ là giải pháp thiết thực, giúp số hóa quy trình vận hành, nâng cao năng lực quản trị và hạn chế tối đa rủi ro thất thoát.

## Mục tiêu của dự án:

Dự án tập trung xây dựng một giải pháp phần mềm toàn diện nhằm giải quyết triệt để bài toán vận hành phòng tập với các mục tiêu cụ thể:

Số hóa quy trình nghiệp vụ lễ tân (POS): Xây dựng giao diện quầy thao tác nhanh, hỗ trợ điểm danh hội viên chính xác, xử lý đăng ký/gia hạn gói tập và bán lẻ tiện ích chỉ trong vài thao tác.

Quản lý chặt chẽ hội viên và dịch vụ: Theo dõi chi tiết thông tin cá nhân, lịch sử tập luyện, trạng thái gói tập (đang hoạt động, sắp hết hạn, hết hạn) và hỗ trợ cảnh báo gia hạn.

Tự động hóa quản lý kho hàng: Đồng bộ hóa quy trình bán lẻ với kho hàng thông qua cơ chế tự động trừ tồn kho ngay khi phát sinh hóa đơn, cảnh báo khi lượng tồn đạt ngưỡng an toàn.

Phân tách và minh bạch báo cáo tài chính: Tự động tổng hợp doanh thu theo từng nguồn độc lập (doanh thu gói tập, doanh thu bán lẻ), theo ca làm việc của nhân viên và theo các mốc thời gian (ngày, tháng, năm), hỗ trợ xuất dữ liệu báo cáo phục vụ công tác kế toán.

Quảng bá hình ảnh phòng tập: Cung cấp kênh thông tin trực tuyến giúp khách hàng dễ dàng tra cứu bảng giá, cơ sở vật chất, đội ngũ huấn luyện viên và gửi yêu cầu tư vấn nhanh chóng.

## Phạm vi triển khai:

Hệ thống được chia thành 3 phân hệ chính tương ứng với từng nhóm người dùng:

Phân hệ Khách hàng (Guest Portal - Website công khai):

Hiển thị trang chủ giới thiệu về quy mô phòng tập, trang thiết bị và huấn luyện viên.

Cung cấp bảng giá chi tiết các gói tập và chương trình ưu đãi hiện hành.

Form tiếp nhận liên hệ, đăng ký nhận tư vấn hoặc trải nghiệm tập thử.

Phân hệ Lễ tân / Nhân viên (Staff):

Điểm danh (Check-in): Tìm kiếm hội viên nhanh theo mã/SĐT, kiểm tra trạng thái thẻ (còn hạn/hết hạn) và ghi nhận lượt vào tập.

Nghiệp vụ gói tập: Tiếp nhận hồ sơ hội viên mới, thực hiện bán gói tập và áp dụng mã khuyến mãi (voucher).

Nghiệp vụ Bán lẻ (Mini POS): Tạo đơn bán nước uống, thực phẩm bổ sung, phụ kiện; áp dụng giảm giá, xuất hóa đơn và tự động trừ tồn kho.

Tra cứu thông tin: Tra cứu lịch sử đăng ký gói tập và lịch sử mua hàng của hội viên tại quầy.

Phân hệ Quản trị (Admin):

Quản lý người dùng & phân quyền: Quản lý tài khoản nhân viên, phân quyền theo vai trò.

Quản lý danh mục dịch vụ & sản phẩm: Cấu hình các gói tập (theo tháng, quý, năm, số buổi), thiết lập danh mục hàng hóa bán lẻ và quản lý nhập/xuất kho.

Quản lý chương trình khuyến mãi: Tạo và phát hành mã voucher giảm giá (theo phần trăm hoặc số tiền cố định).

Báo cáo & Thống kê: Dashboard trực quan hóa dòng tiền, biểu đồ tăng trưởng hội viên, top dịch vụ/sản phẩm mang lại doanh thu cao nhất.

## Đối tượng thụ hưởng:

Hệ thống mang lại giá trị thực tế cho các nhóm đối tượng tham gia vào quá trình vận hành:

Chủ phòng gym / Ban quản trị: Nắm bắt chính xác tình hình kinh doanh, dòng tiền và lượng tồn kho theo thời gian thực; loại bỏ nguy cơ thất thoát tài chính và đưa ra quyết định quản trị chuẩn xác dựa trên dữ liệu.

Nhân viên lễ tân / Nhân viên quầy: Tinh gọn các thao tác nghiệp vụ, giảm bớt áp lực tính toán thủ công, xử lý điểm danh và bán hàng nhanh chóng, nâng cao hiệu suất làm việc.

Khách hàng / Hội viên: Nhận được sự phục vụ chuyên nghiệp, minh bạch về quyền lợi thẻ tập, thời hạn sử dụng và thuận tiện trong việc tra cứu bảng giá cũng như các chương trình ưu đãi của phòng gym.

# CHƯƠNG 2: KIẾN TRÚC HỆ THỐNG & CÔNG NGHỆ ÁP DỤNG

## Môi trường triển khai:

Hệ thống Quản lý phòng tập Gym được xây dựng theo kiến trúc 3-Layer (Presentation – Business Logic – Data Access) dưới dạng ứng dụng monolithic, cho phép người dùng truy cập trực tiếp thông qua các trình duyệt web phổ biến trên máy tính quầy lễ tân lẫn thiết bị di động mà không cần cài đặt thêm phần mềm phụ trợ. Phần giao diện người dùng (Frontend) được triển khai trên nền tảng đám mây Vercel, trong khi phần máy chủ dịch vụ (Backend) được triển khai trên một máy chủ/VPS hỗ trợ môi trường Java (JVM) nhằm đảm bảo khả năng duy trì kết nối liên tục tới cơ sở dữ liệu và đáp ứng yêu cầu về hiệu năng, xử lý đồng thời nhiều phiên thao tác tại quầy.

## Công nghệ phát triển:

Backend: Java Spring Boot, RESTful API

Frontend: ReactJS, HTML5, CSS3, Tailwind CSS, JavaScript

Database: MySQL

Authentication: JWT

## Công cụ hỗ trợ phát triển & Quản trị dự án:

Quản lý mã nguồn: Github

Quản lý testcase: Zephyr for Jira

Môi trường dev: Visual Studio Code, Intellij IDEA

UI : Balsamiq

Vẽ biểu đồ: Draw.io

PM: Jira

Quản lý tài liệu dự án: Drive, Google Docs

Giao tiếp đội nhóm: Jira, Messenger

# CHƯƠNG 3: PHÂN TÍCH & ĐẶC TẢ YÊU CẦU NGHIỆP VỤ

## Phân loại tác nhân (Actors Identification):

Khách hàng (Guest), Nhân viên lễ tân (Staff), Quản trị viên (Admin)

## Danh mục các ca sử dụng (Use Case Catalog):

| STT | Ca sử dụng | Mô tả ngắn | Tác nhân |
| --- | --- | --- | --- |
| 1 | Xem trang chủ & dịch vụ | Xem hình ảnh, tiện ích, giới thiệu dịch vụ (Gym, Yoga, PT) | Khách hàng |
| 2 | Xem bảng giá & thông tin gói tập | Xem danh sách các gói tập cơ bản | Khách hàng |
| 3 | Liên hệ tư vấn | Chuyển hướng liên hệ qua<br>Hotline/Zalo/Messenger | Khách hàng |
| 4 | Đăng nhập | Đăng nhập tài khoản làm việc | Staff, Admin |
| 5 | Đăng xuất | Thoát khỏi phiên làm việc hiện<br>tại | Staff, Admin |
| 6 | Quản lý hội viên | Thêm mới, tra cứu, cập nhật<br>thông tin hội viên | Staff |
| 7 | Bán gói tập | Đăng ký gói tập cho khách, áp<br>mã voucher, xuất hóa đơn | Staff |
| 8 | Điểm danh hội<br>viên (Check-in) | Kiểm tra hạn thẻ của hội viên và<br>lưu lịch sử vào tập | Staff |
| 9 | Bán hàng bán lẻ<br>(POS) | Chọn nước/đồ tập, thanh toán<br>hóa đơn, tự trừ kho | Staff |
| 10 | Quản lý nhân<br>viên | Thêm, sửa thông tin, đổi mật<br>khẩu, khóa tài khoản Staff | Admin |
| 11 | Quản lý gói tập | Thêm, sửa giá/thời hạn, xóa hoặc<br>ẩn các gói tập | Admin |
| 12 | Quản lý kho &<br>hàng hóa | Quản lý danh mục sản phẩm, giá<br>bán, nhập thêm số lượng tồn | Admin |
| 13 | Quản lý khuyến<br>mãi | Tạo mã voucher giảm giá, cấu<br>hình hạn dùng và mức giảm | Admin |
| 14 | Thống kê doanh<br>thu | Xem báo cáo tổng thu, phân tách<br>doanh thu Dịch vụ và Bán lẻ | Admin |

*Bảng 3.1. Bảng xác định các ca sử dụng*

## Sơ đồ Use Case tổng quan

Sơ đồ Use Case tổng quan:

![Hình ảnh](images_srs/image2.png)

*Hình 3.1. Biểu đồ Use Case tổng quan*

## Đặc tả chi tiết Use Case (Kèm Activity Diagram & UI Wireframe):

Mỗi Use Case gồm 3 thành phần liên kết chặt chẽ:

Biểu đồ hoạt động (Activity Diagram).

*Bảng đặc tả use case chi tiết (Tác nhân, Tiền/Hậu điều kiện, Luồng sự kiện chính, Luồng ngoại lệ).*

Thiết kế giao diện tương ứng (UI Wireframe).

Danh sách các Use Case cần đặc tả chi tiết:

### Xem trang chủ & dịch vụ

Biểu đồ hoạt động:

![Hình ảnh](images_srs/image3.jpeg)

*Hình 3.2. Biểu đồ hoạt động của UC01: Xem trang chủ & dịch vụ*

Đặc tả Use Case:

| Thuộc tính | Nội dung |
| --- | --- |
| Use Case ID | UC01 |
| Tên | Xem trang chủ & dịch vụ |
| Tác nhân (Actor) | Khách hàng |
| Mô tả | Cho phép khách hàng truy cập vào trang chủ và xem danh sách các dịch vụ mà hệ thống cung cấp. |
| Tiền điều kiện | Thiết bị kết nối internet |
| Hậu điều kiện | Hệ thống tải và hiển thị đầy đủ giao diện trang chủ cùng các dịch vụ của phòng tập |
| Luồng chính | Khách hàng truy cập vào trang web/ứng dụng<br>Hệ thống hiển thị giao diện trang chủ, hình ảnh, tiện ích, giới thiệu dịch vụ (Gym, Yoga, PT)<br>Khách hàng chọn xem chi tiết một dịch vụ cụ thể<br>Hệ thống tải và hiển thị thông tin chi tiết nội dung dịch vụ |
| Luồng thay thế | 1a. Không tìm thấy trang website / mất kết nối: Hệ thống hiển thị thông báo lỗi và yêu cầu kiểm tra lại mạng |

*Bảng 3.2. Đặc tả UC01: Xem trang chủ & dịch vụ*

Giao diện UI (demo):

![Hình ảnh](images_srs/image4.jpeg)

*Hình 3.2.1. Giao diện UI Demo của UC01: Xem trang chủ & dịch vụ*

### Xem bảng giá & Xem thông tin gói tập

Biểu đồ hoạt động:

![Hình ảnh](images_srs/image5.png)

*Hình 3.3. Biểu đồ hoạt động của UC02: Xem bảng giá & thông tin gói tập*

Đặc tả Use case:

| Thuộc tính | Nội dung |
| --- | --- |
| Use Case ID | UC02 |
| Tên | Xem bảng giá & Xem thông tin gói tập |
| Tác nhân (Actor) | Khách hàng |
| Mô tả | Cho phép khách hàng tra cứu bảng giá và xem chi tiết thông tin của các gói tập. |
| Tiền điều kiện | Khách hàng đang ở trang chủ hoặc trang dịch vụ |
| Hậu điều kiện | Hệ thống hiển thị thành công danh sách bảng giá và chi tiết gói tập |
| Luồng chính | Khách hàng chọn mục "Bảng giá" hoặc "Gói tập"<br>Hệ thống hiển thị danh sách các gói tập và bảng giá tương ứng<br>Khách hàng click chọn một gói tập cụ thể<br>Hệ thống hiển thị chi tiết quyền lợi/chính sách của gói tập |
| Luồng thay thế | 2a. Dữ liệu đang cập nhật: Hệ thống hiển thị thông báo dữ liệu đang được cập nhật nếu chưa có thông tin gói tập |

*Bảng 3.3. Đặc tả UC02: Xem bảng giá & thông tin gói tập*

Giao diện UI(demo):

![Hình ảnh](images_srs/image6.jpeg)

*Hình 3.3.1. Giao diện UI Demo của UC02: Xem bảng giá & thông tin gói*

tập

### Liên hệ tư vấn nhanh

Biểu đồ hoạt động:

![Hình ảnh](images_srs/image7.jpeg)

*Hình 3.4. Biểu đồ hoạt động của UC03: Liên hệ tư vấn nhanh*

Đặc tả Use Case:

| Thuộc tính | Nội dung |
| --- | --- |
| Use Case ID | UC03 |
| Tên | Liên hệ tư vấn nhanh (Hotline/Zalo/Messenger) |
| Tác nhân (Actor) | Khách hàng |
| Mô tả | Cho phép khách hàng liên hệ trực tiếp với nhân viên tư vấn qua các kênh Hotline, Zalo, hoặc Messenger. |
| Tiền điều kiện | Thiết bị kết nối internet, có cài đặt ứng dụng hoặc hỗ trợ gọi điện |
| Hậu điều kiện | Khách hàng được chuyển hướng đến kênh liên lạc tương ứng |
| Luồng chính | Khách hàng nhấn vào nút "Liên hệ tư vấn" trên giao diện<br>Hệ thống hiển thị popup chứa 3 lựa chọn (Hotline/Zalo/Messenger)<br>Khách hàng chọn 1 kênh liên hệ cụ thể<br>Hệ thống yêu cầu xác nhận mở ứng dụng (nếu cần thiết)<br>Hệ thống chuyển hướng người dùng đến ứng dụng tương ứng hoặc gọi điện thoại trực tiếp |
| Luồng thay thế | 2a. Khách hàng bấm nút đóng popup/hủy thao tác: Hệ thống đóng popup và giữ nguyên màn hình hiện tại<br>4a. Ứng dụng (Zalo/Messenger) chưa được cài đặt: Hệ thống điều hướng đến trang tải ứng dụng hoặc trang web tương ứng của nền tảng đó |

*Bảng 3.4. Đặc tả UC03: Liên hệ tư vấn nhanh*

Giao diện UI(demo):

![Hình ảnh](images_srs/image8.jpeg)

*Hình 3.4.1. Giao diện UI Demo của UC03: Liên hệ tư vấn nhanh*

### Đăng nhập

Biểu đồ hoạt động:

![Hình ảnh](images_srs/image9.jpeg)

*Hình 3.5. Biểu đồ hoạt động của UC04: Đăng nhập*

Đặc tả Use Case:

| Thuộc tính | Nội dung |
| --- | --- |
| Use Case ID | UC04 |
| Tên | Đăng nhập |
| Tác nhân (Actor) | Nhân viên, Admin |
| Mô tả | Cho phép nhân viên, admin đăng nhập vào hệ thống để sử dụng các tính năng theo vai trò. |
| Tiền điều kiện | Nhân viên, admin đã có tài khoản trên hệ thống |
| Hậu điều kiện | Nhân viên, admin đăng nhập thành công và chuyển hướng đến màn hình tổng quan theo phân quyền Admin/Staff |
| Luồng chính | Nhân viên, admin chọn chức năng "Đăng nhập"<br>Hệ thống hiển thị form đăng nhập<br>Nhân viên, admin nhập thông tin tài khoản (Số điện thoại/Email) và mật khẩu<br>Nhân viên, admin nhấn nút "Đăng nhập"<br>Hệ thống kiểm tra dữ liệu<br>Hệ thống đăng nhập thành công và chuyển hướng đến màn hình tổng quan theo phân quyền Admin/Staff |
| Luồng thay thế | 5a. Sai tên đăng nhập hoặc mật khẩu: Hệ thống thông báo lỗi và yêu cầu nhập lại<br>5b. Tài khoản đang bị khóa: Hệ thống thông báo lỗi tài khoản bị khóa |

*Bảng 3.5. Đặc tả UC04: Đăng nhập*

Giao diện UI(demo):

![Hình ảnh](images_srs/image10.png)

*Hình 3.5.1. Giao diện UI Demo của UC04: Đăng nhập*

### Đăng xuất

Biểu đồ hoạt động:

![Hình ảnh](images_srs/image11.jpeg)

*Hình 3.6. Biểu đồ hoạt động của UC05: Đăng xuất*

Đặc tả Use Case:

| Thuộc tính | Nội dung |
| --- | --- |
| Use Case ID | UC05 |
| Tên | Đăng xuất |
| Tác nhân (Actor) | Nhân viên, admin đã đăng nhập |
| Mô tả | Cho phép Nhân viên, admin thoát khỏi phiên đăng nhập hiện tại trên hệ thống. |
| Tiền điều kiện | Nhân viên, admin đã đăng nhập thành công vào hệ thống |
| Hậu điều kiện | Phiên đăng nhập kết thúc, hệ thống chuyển hướng về trang đăng nhập |
| Luồng chính | Nhân viên, admin chọn chức năng "Đăng xuất" từ menu cá nhân<br>Hệ thống hiển thị popup xác nhận đăng xuất<br>Nhân viên, admin nhấn chọn "Xác nhận"<br>Hệ thống hủy phiên làm việc hiện tại<br>Hệ thống thông báo thành công và chuyển hướng về Màn hình Đăng nhập |
| Luồng thay thế | 3a. Người dùng bấm "Hủy bỏ" trên popup xác nhận: Hệ thống đóng popup và giữ nguyên màn hình hiện tại<br>4a. Lỗi hệ thống/mất kết nối mạng: Hệ thống hiển thị thông báo lỗi không thể hủy phiên làm việc |

*Bảng 3.6. Đặc tả UC05: Đăng xuất*

Giao diện UI(demo):

![Hình ảnh](images_srs/image12.jpeg)

*Hình 3.6.1. Giao diện UI Demo của UC05: Đăng xuất*

### Quản lý hội viên

Biểu đồ hoạt động:

![Hình ảnh](images_srs/image13.png)

*Hình 3.7. Biểu đồ hoạt động của UC06: Quản lý hội viên*

Đặc tả Use Case:

| Thuộc tính | Nội dung |
| --- | --- |
| Use Case ID | UC06 |
| Tên | Quản lý hội viên |
| Tác nhân (Actor) | Nhân viên (Staff) |
| Mô tả | Cho phép Nhân viên thêm mới hồ sơ hội viên vào hệ thống, tra cứu thông tin và cập nhật dữ liệu của hội viên khi có thay đổi. |
| Tiền điều kiện | Nhân viên đã đăng nhập thành công và truy cập vào trang Quản lý hội viên. |
| Hậu điều kiện | Thông tin hội viên được tạo mới hoặc cập nhật thành công vào cơ sở dữ liệu. |
| Luồng chính (Cập nhật) | Nhân viên nhập từ khóa tìm kiếm (Tên, SĐT, Mã hội viên).<br>Hệ thống hiển thị danh sách hội viên phù hợp.<br>Nhân viên chọn hội viên cần chỉnh sửa.<br>Hệ thống hiển thị form thông tin chi tiết.<br>Nhân viên thay đổi thông tin và nhấn "Cập nhật".<br>Hệ thống xác thực dữ liệu, cập nhật vào DB và thông báo thành công. |
| Luồng phụ (Thêm mới) | 1a. Tại màn hình danh sách, Nhân viên nhấn nút "Thêm mới". 2a. Hệ thống hiển thị form nhập thông tin hội viên.<br>3a. Nhân viên điền thông tin và nhấn "Lưu".<br>4a. Hệ thống xác thực dữ liệu, lưu vào DB và thông báo thành công. |
| Luồng thay thế | 1a. Dữ liệu không hợp lệ: (Ví dụ: để trống trường bắt buộc, sai định dạng email/SĐT). Hệ thống báo lỗi màu đỏ tại trường tương ứng và yêu cầu nhập lại, không lưu DB.<br>2a. Không tìm thấy kết quả: Khi tra cứu, nếu từ khóa không khớp, hệ thống hiển thị thông báo "Không tìm thấy hội viên nào phù hợp". |

*Bảng 3.7. Đặc tả UC06: Quản lý hội viên*

Giao diện UI(demo):

![Hình ảnh](images_srs/image14.jpeg)

*Hình 3.7.1. Giao diện UI Demo của UC06: Quản lý hội viên*

### Bán gói tập (Membership)

Biểu đồ hoạt động:

![Hình ảnh](images_srs/image15.png)

*Hình 3.8. Biểu đồ hoạt động của UC07: Bán gói tập*

Đặc tả Use Case:

| Thuộc tính | Nội dung |
| --- | --- |
| Use Case ID | UC07 |
| Tên | Bán gói tập |
| Tác nhân (Actor) | Nhân viên (Staff) |
| Mô tả | Cho phép Nhân viên thực hiện quy trình đăng ký gói tập cho hội viên, áp dụng mã giảm giá, nhận thanh toán và in hóa đơn. |
| Tiền điều kiện | Nhân viên đã đăng nhập thành công.<br>Hội viên đã có hồ sơ trên hệ thống (nếu chưa có phải thực hiện UC06 trước). |
| Hậu điều kiện | Gói tập được kích hoạt cho hội viên. Thông tin hóa đơn/thanh toán được lưu trữ thành công. |
| Luồng chính | Nhân viên tìm chọn Hội viên và chọn chức năng "Bán gói tập".<br>Nhân viên chọn gói tập tương ứng và nhập mã Voucher (nếu khách có).<br>Hệ thống kiểm tra Voucher hợp lệ và tính toán tổng số tiền thanh toán cuối cùng.<br>Nhân viên chọn phương thức thanh toán.<br>Nhân viên thu tiền từ khách và bấm "Xác nhận thanh toán".<br>Hệ thống lưu hóa đơn vào DB, kích hoạt gói tập và hiển thị thông báo thành công.<br>Nhân viên bấm "In hóa đơn" (tùy chọn) và giao cho khách. |
| Luồng thay thế | 3a. Mã Voucher không hợp lệ: Hệ thống báo lỗi (Voucher hết hạn, sai mã, hết lượt dùng) và yêu cầu xóa mã hoặc giữ nguyên giá gốc.<br>5a. Hủy giao dịch: Nhân viên bấm "Hủy" trước khi xác nhận thanh toán, hệ thống đóng form và không ghi nhận dữ liệu.<br>6a. Lỗi hệ thống/Mạng: Đang thanh toán bị rớt mạng, hệ thống sẽ rollback (hoàn tác) giao dịch, hiển thị lỗi yêu cầu thực hiện lại. |

*Bảng 3.8. Đặc tả UC07: Bán gói tập*

Giao diện UI(demo):

![Hình ảnh](images_srs/image16.jpeg)

*Hình 3.8.1. Giao diện UI Demo của UC07: Bán gói tập*

### Điểm danh hội viên (Check-in)

Biểu đồ hoạt động:

![Hình ảnh](images_srs/image17.jpeg)

*Hình 3.9. Biểu đồ hoạt động của UC08: Điểm danh hội viên*

Đặc tả Use Case:

| Thuộc tính | Nội dung |
| --- | --- |
| Use Case ID | UC08 |
| Tên | Điểm danh hội viên (Check-in) |
| Tác nhân (Actor) | Nhân viên (Staff) |
| Mô tả | Cho phép Nhân viên điểm danh hội viên đến phòng tập bằng cách nhập mã hoặc quét thẻ; hệ thống tự động kiểm tra hạn gói tập và lưu lại lịch sử. |
| Tiền điều kiện | Nhân viên đã đăng nhập và đang mở màn hình Điểm danh (Check-in). |
| Hậu điều kiện | Lịch sử đến tập (Check-in) được lưu vào DB. Hệ thống tự động trừ đi 1 buổi tập (nếu khách dùng gói theo lượt/buổi). |
| Luồng chính | Nhân viên nhập mã hội viên (hoặc dùng máy quét mã thẻ/QR).<br>Hệ thống tìm kiếm thông tin và truy xuất gói tập hiện tại của hội viên.<br>Hệ thống kiểm tra gói tập còn hạn và còn số buổi sử dụng (nếu là gói theo buổi).<br>Hệ thống ghi nhận lịch sử Check-in, hiển thị thông báo "Thành công" kèm thông tin/hình ảnh hội viên lên màn hình.<br>Hệ thống trừ tự động trừ 1 nếu là gói tập theo buổi. |
| Luồng thay thế | 1a. Không tìm thấy thẻ: Nhập sai mã hoặc quét thẻ chưa đăng ký, hệ thống báo lỗi "Không tìm thấy thông tin hội viên".<br>3a. Thẻ hết hạn/Hết buổi tập: Khi hệ thống kiểm tra thấy gói tập đã hết hạn (EndDate) hoặc hết lượt, hệ thống từ chối Check-in, hiển thị cảnh báo đỏ và phát âm thanh lỗi. Nhân viên tiến hành mời khách mua thêm gói hoặc gia hạn.<br>2a. Tài khoản bị khóa: Nếu khách hàng vi phạm thì hệ thống sẽ cảnh báo "Tài khoản đang bị khóa ". |

*Bảng 3.9. Đặc tả UC08: Điểm danh hội viên*

Giao diện UI(demo):

![Hình ảnh](images_srs/image18.jpeg)

*Hình 3.9.1. Giao diện UI Demo của UC08: Điểm danh hội viên*

### Bán hàng bán lẻ (Mini POS)

Biểu đồ hoạt động:

![Hình ảnh](images_srs/image19.png)

*Hình 3.10. Biểu đồ hoạt động của UC09: Bán hàng bán lẻ (Mini POS)*

Đặc tả Use Case:

| Thuộc tính | Nội dung |
| --- | --- |
| Use Case ID | UC09 |
| Tên | Bán hàng bán lẻ (POS) |
| Tác nhân (Actor) | Nhân viên (Staff) |
| Mô tả | Cho phép nhân viên chọn sản phẩm bán lẻ (nước uống, đồ tập,…), lập hóa đơn thanh toán cho khách hàng và tự động trừ số lượng tồn kho sau khi thanh toán thành công |
| Tiền điều kiện | Nhân viên đã đăng nhập thành công vào hệ thống |
| Hậu điều kiện | Hóa đơn bán hàng được tạo và số lượng tồn kho được cập nhật tương ứng |
| Luồng chính | Nhân viên mở chức năng "Bán hàng" (POS)<br>Hệ thống hiển thị danh mục sản phẩm/tồn kho hiện có 3. Nhân viên chọn sản phẩm và thêm vào giỏ hàng<br>Nhân viên xác nhận giỏ hàng<br>Hệ thống kiểm tra tồn kho từng sản phẩm trong giỏ hàng<br>Hệ thống tính tổng tiền<br>Nhân viên chọn hình thức thanh toán (tiền mặt/QR) và bấm xác nhận<br>Hệ thống lưu hóa đơn và tự động trừ tồn kho tương ứng<br>Hệ thống thông báo thực hiện thành công |
| Luồng thay thế | 4a: Giỏ hàng trống: Không cho xác nhận, yêu cầu chọn sản phẩm<br>5a: Không đủ tồn kho: Hiển thị cảnh báo, yêu cầu điều chỉnh giỏ hàng<br>7a: Khách hàng đổi ý, yêu cầu hủy giao dịch giữa chừng: NV chọn hủy, hệ thống hủy giỏ hàng (không trừ tồn kho, không tạo hóa đơn), quay về màn hình chọn sản phẩm |

*Bảng 3.10. Đặc tả UC09: Bán hàng bán lẻ (Mini POS)*

Giao diện UI(demo):

![Hình ảnh](images_srs/image20.jpeg)

*Hình 3.10.1. Giao diện UI Demo của UC09: Bán hàng bán lẻ (Mini POS)*

### Quản lý nhân viên

Biểu đồ hoạt động:

![Hình ảnh](images_srs/image21.png)

*Hình 3.11. Biểu đồ hoạt động của UC10: Quản lý nhân viên*

Đặc tả Use Case:

| Thuộc tính | Nội dung |
| --- | --- |
| Use Case ID | UC10 |
| Tên | Quản lý nhân viên |
| Tác nhân (Actor) | Admin |
| Mô tả | Chức năng cho phép Admin xem danh sách, tìm kiếm, cập nhật thông tin chi tiết, thêm nhân viên mới hoặc thực hiện khóa/mở tài khoản của nhân viên trong hệ thống. |
| Tiền điều kiện | Tài khoản Admin đang được đăng nhập hợp lệ trong hệ thống |
| Hậu điều kiện | Dữ liệu hồ sơ/tài khoản nhân viên được tạo mới hoặc cập nhật thành công vào CSDL |
| Luồng chính | Admin chọn chức năng quản lý nhân viên trên sidebar<br>Hệ thống truy xuất cơ sở dữ liệu và hiển thị danh sách nhân viên<br>Admin tìm kiếm và chọn một nhân viên cụ thể để xem/sửa thông tin<br>Hệ thống Hiển thị form chứa thông tin chi tiết của nhân viên đó<br>Admin nhập các thông tin cần thiết và xác nhận (Submit)<br>Hệ thống kiểm tra tính hợp lệ của dữ liệu, lưu vào cơ sở dữ liệu<br>Hệ thống hiển thị thông báo cập nhật nhân viên thành công |
| Luồng thay thế | 3a. Thêm nhân viên mới<br>Admin: chọn nút "Thêm mới" trên giao diện danh sách.<br>Hệ thống: hiển thị Form nhập liệu trống.<br>Admin: điền thông tin nhân viên mới và xác nhận.<br>Hệ thống: quay lại bước 6 của luồng chính để validate và lưu.<br>3b. Khóa/Mở tài khoản nhân viên |
|  | Admin: Bấm nút Khóa/Mở tài khoản tại dòng thông tin của một nhân viên<br>Hệ thống: Hiển thị popup yêu cầu xác nhận hành động<br>Admin: Xác nhận Khóa/Mở<br>Hệ thống: Cập nhật trạng thái tài khoản vào CSDL và thông báo thành công<br>4a. Hệ thống hiển thị thông báo: Không tìm thấy nhân viên phù hợp<br>6a. thống hiển thị thông báo lỗi validate (trùng email, mật khẩu yếu...) và yêu cầu nhập lại<br>6b. Admin hủy thao tác giữa chừng và đóng Form nhập liệu |

*Bảng 3.11. Đặc tả UC10: Quản lý nhân viên*

Giao diện UI(demo):

![Hình ảnh](images_srs/image22.jpeg)

*Hình 3.11.1. Giao diện UI Demo của UC10: Quản lý nhân viên*

### Quản lý gói tập

Biểu đồ hoạt động:

![Hình ảnh](images_srs/image23.png)

*Hình 3.12. Biểu đồ hoạt động của UC11: Quản lý gói tập*

Đặc tả Use Case:

| Thuộc tính | Nội dung |
| --- | --- |
| Use Case ID | UC11 |
| Tên | Quản lý gói tập |
| Tác nhân (Actor) | Admin |
| Mô tả | Chức năng giúp Admin quản lý danh mục các gói tập dịch vụ của phòng gym, bao gồm việc xem chi tiết, chỉnh sửa thông tin gói hiện tại, thêm gói mới và xóa các gói không còn kinh doanh. |
| Tiền điều kiện | Tài khoản Admin đang được đăng nhập hợp lệ trong hệ thống |
| Hậu điều kiện | Danh sách và thông tin gói tập được cập nhật vào cơ sở dữ liệu của hệ thống |
| Luồng chính | Admin chọn chức năng quản lý gói tập<br>Hệ thống hiển thị danh sách các gói tập hiện có<br>Admin tìm kiếm gói tập cần xem hoặc chỉnh sửa<br>Hệ thống kiểm tra xem có gói tập hay không và hiển thị kết quả<br>Admin chỉnh sửa dữ liệu (Tên, Thời hạn, Số buổi, Giá tiền...) và nhấn lưu<br>Hệ thống kiểm tra tính hợp lệ của các dữ liệu vừa nhập<br>Hệ thống cập nhật dữ liệu và hiển thị thông báo thao tác thành công |
| Luồng thay thế | 3a. Thêm gói tập mới:<br>Admin bấm chọn nút "Thêm mới"<br>Hệ thống hiển thị Form tạo gói tập trống<br>Admin nhập thông tin gói tập mới và nhấn lưu<br>Hệ thống quay lại bước 6 của luồng chính để validate và thêm vào CSDL |
|  | 3b. Xóa gói tập<br>Admin chọn hành động "Xóa" trên một gói tập cụ thể<br>Hệ thống hiển thị cảnh báo xác nhận việc xóa<br>Admin xác nhận xóa<br>Hệ thống xóa (hoặc chuyển trạng thái ngừng bán) gói tập, thông báo thành công<br>3b1. Lỗi xóa gói tập đang có hội viên sử dụng: Hệ thống hiển thị thông báo không thể xóa do có hội viên đang đăng ký gói này<br>6a. Lỗi Validate Form: Hệ thống phát hiện giá tiền âm (<0), số buổi không hợp lệ (<=0) hoặc các trường bắt buộc bị bỏ trống. Hệ thống chặn lưu, báo lỗi đỏ ở trường tương ứng và yêu cầu Admin sửa lại. |

*Bảng 3.12. Đặc tả UC11: Quản lý gói tập*

Giao diện UI(demo):

![Hình ảnh](images_srs/image24.jpeg)

*Hình 3.12.1. Giao diện UI Demo của UC11: Quản lý gói tập*

### Quản lý kho & hàng hóa

Biểu đồ hoạt động:

![Hình ảnh](images_srs/image25.png)

*Hình 3.13. Biểu đồ hoạt động của UC12: Quản lý kho & hàng hóa*

Đặc tả Use Case:

| Thuộc tính | Nội dung |
| --- | --- |
| Use Case ID | UC12 |
| Tên | Quản lý danh mục hàng hóa & Nhập kho |
| Tác nhân (Actor) | Quản trị viên (Admin) |
| Mô tả | Cho phép Admin quản lý danh mục sản phẩm bán tại quầy và giá bán, xem tổng quan và điều chỉnh số lượng tồn kho |
| Tiền điều kiện | Admin đã đăng nhập thành công và có quyền quản trị hệ thống |
| Hậu điều kiện | Danh mục sản phẩm/giá bán được cập nhật, hoặc số lượng tồn kho và trạng thái được cập nhật |
| Luồng chính | Admin mở màn hình "Hàng hóa"/"Kho"<br>Hệ thống hiển thị danh sách hiện có<br>Admin thêm/sửa sản phẩm, nhập thông tin<br>Hệ thống kiểm tra hợp lệ<br>Hệ thống cập nhật danh mục<br>Hệ thống thông báo thành công |
| Luồng phụ | 3a. Điều chỉnh tồn kho (Nhập / Xuất kho):<br>Admin: tìm kiếm và chọn sản phẩm cần điều chỉnh tồn kho trong danh sách.<br>Hệ thống: hiển thị hộp thoại/form "Điều chỉnh số lượng tồn kho" kèm số lượng hiện tại.<br>Admin: chọn loại thao tác (Nhập kho thêm hoặc Xuất/Hủy hàng) và điền số lượng cần điều chỉnh kèm lý do (nếu có).<br>Hệ thống: kiểm tra tính hợp lệ của dữ liệu (số lượng nhập/xuất phải là số nguyên dương; nếu là xuất kho thì số lượng xuất không vượt quá số lượng tồn hiện có).<br>Hệ thống thực hiện cộng/trừ số lượng tồn kho trong CSDL, tự động cập nhật lại trạng thái sản phẩm (Còn hàng / Sắp hết hàng /<br>Hết hàng). |
|  | Hệ thống hiển thị thông báo: "Cập nhật tồn kho thành công". 3b. Xóa sản phẩm:<br>Admin nhấn nút Xóa tại dòng sản phẩm tương ứng trong danh sách.<br>Hệ thống hiển thị popup yêu cầu xác nhận: "Bạn có chắc chắn muốn xóa sản phẩm này không?".<br>Admin chọn Xác nhận xóa.<br>Hệ thống kiểm tra ràng buộc dữ liệu:<br>+ Trường hợp sản phẩm đã từng phát sinh giao dịch/hóa đơn POS: Hệ thống chuyển sang cơ chế Xóa mềm (Soft Delete / Ẩn sản phẩm) để bảo toàn tính toàn vẹn dữ liệu lịch sử hóa đơn.<br>+ Trường hợp sản phẩm chưa có giao dịch: Hệ thống thực hiện xóa khỏi danh mục hàng hóa trong CSDL.<br>Hệ thống hiển thị thông báo: "Xóa sản phẩm thành công" và tải lại danh sách. |
| Luồng thay thế | 3a1. Xuất kho vượt tồn → từ chối, báo lỗi, nhập lại<br>3b1. SP đã bán → không cho xóa, gợi ý chuyển "Ngừng kinh doanh"<br>4a. Trùng tên/mã → báo lỗi, nhập lại (về bước 3)<br>4b. Dữ liệu không hợp lệ (giá/số lượng âm) → báo lỗi, nhập lại (về bước 3) |

*Bảng 3.13. Đặc tả UC12: Quản lý kho & hàng hóa*

Giao diện UI(demo):

![Hình ảnh](images_srs/image26.jpeg)

![Hình ảnh](images_srs/image27.jpeg)

*Hình 3.13.1. Giao diện UI Demo của UC12: Quản lý kho & hàng hóa*

### Quản lý khuyến mãi

Biểu đồ hoạt động:

![Hình ảnh](images_srs/image28.png)

*Hình 3.14. Biểu đồ hoạt động của UC13: Quản lý khuyến mãi*

Đặc tả Use Case:

| Thuộc tính | Nội dung |
| --- | --- |
| Use Case ID | UC13 |
| Tên | Quản lý khuyến mãi |
| Tác nhân (Actor) | Quản trị viên (Admin) |
| Mô tả | Cho phép Admin tạo mới các mã voucher/chiến dịch khuyến mãi, cấu hình mức giảm giá, điều kiện áp dụng và thời hạn sử dụng |
| Tiền điều kiện | Admin đã đăng nhập thành công và có quyền quản trị hệ thống |
| Hậu điều kiện | Chiến dịch khuyến mãi/voucher mới được lưu và ở trạng thái "Đang chạy" (nếu hợp lệ) |
| Luồng chính | Admin chọn "Quản lý khuyến mãi"<br>Hệ thống hiển thị danh sách voucher/chiến dịch hiện có<br>Admin chọn "Tạo voucher mới"<br>Hệ thống hiển thị Form tạo mã khuyến mãi<br>Admin nhập thông tin và bấm "Lưu"<br>Hệ thống kiểm tra tính hợp lệ của dữ liệu<br>Hệ thống lưu vào cơ sở dữ liệu, kích hoạt chiến dịch khuyến mãi và hiển thị thông báo tạo khuyến mãi thành công |
| Luồng thay thế | 5a. Admin đóng form hoặc hủy thao tác giữa chừng → Hệ thống hủy dữ liệu đang nhập, không lưu, và quay về màn hình danh sách voucher/chiến dịch<br>6a. Mã voucher đã tồn tại → hệ thống thông báo trùng mã và yêu cầu nhập mã khác<br>6b. Hạn dùng hoặc mức giảm không hợp lệ → hệ thống thông báo lỗi tương ứng |

*Bảng 3.14. Đặc tả UC13: Quản lý khuyến mãi*

Giao diện UI(demo):

![Hình ảnh](images_srs/image29.jpeg)

*Hình 3.14.1. Giao diện UI Demo của UC13: Quản lý khuyến mãi*

### Thống kê doanh thu

Biểu đồ hoạt động:

![Hình ảnh](images_srs/image30.png)

*Hình 3.15. Biểu đồ hoạt động của UC14: Thống kê doanh thu*

Đặc tả Use Case:

| Thuộc tính | Nội dung |
| --- | --- |
| Use Case ID | UC14 |
| Tên | Thống kê doanh thu |
| Tác nhân (Actor) | Admin |
| Mô tả | Chức năng hỗ trợ Admin xem báo cáo tổng hợp doanh thu theo thời gian tùy chọn, phân tách rõ nguồn thu từ gói tập và bán lẻ, đồng thời hỗ trợ xuất dữ liệu ra file Excel/PDF để lưu trữ. |
| Tiền điều kiện | Tài khoản có quyền Admin đang được đăng nhập |
| Hậu điều kiện | Hệ thống hiển thị chính xác số liệu doanh thu dựa trên yêu cầu lọc của người dùng |
| Luồng chính | Admin chọn chức năng Thống kê & Báo cáo<br>Hệ thống hiển thị giao diện tổng quan với số liệu mặc định (tháng hiện tại)<br>Admin thiết lập khoảng thời gian (Từ ngày - Đến ngày) và các tiêu chí lọc khác<br>Admin nhấn nút "Lọc/Xem báo cáo"<br>Hệ thống kiểm tra tính hợp lệ của mốc thời gian.<br>Hệ thống truy vấn cơ sở dữ liệu tính toán tổng thu nhập.<br>Hệ thống hiển thị biểu đồ phân tách và bảng dữ liệu chi tiết lên màn hình |
| Luồng thay thế | 5a. Khoảng thời gian không hợp lệ: Hệ thống phát hiện "Từ ngày" lớn hơn "Đến ngày". Hệ thống hiển thị popup cảnh báo lỗi thời gian không hợp lệ và yêu cầu chọn lại.<br>6a. Không có dữ liệu: Hệ thống trả về biểu đồ trống và thông báo: "Không có dữ liệu giao dịch trong khoảng thời gian được chọn".<br>7a. Xuất file (Excel/PDF) |
|  | Admin nhấn nút "Xuất Excel" hoặc "Xuất PDF" trên giao diện kết quả<br>Hệ thống thu thập bảng dữ liệu đang hiển thị, định dạng thành file Excel/PDF<br>Hệ thống tự động tải file báo cáo về thiết bị của người dùng |

*Bảng 3.15. Đặc tả UC14: Thống kê doanh thu*

Giao diện UI(demo):

![Hình ảnh](images_srs/image31.jpeg)

*Hình 3.15.1. Giao diện UI Demo của UC14: Thống kê doanh thu*

## Yêu cầu phi chức năng (Non-functional Requirements):

Hiệu năng & Tải: Thời gian phản hồi nghiệp vụ quầy dưới 1.5s, hỗ trợ nhiều phiên thao tác POS đồng thời.

Tính bảo mật & Toàn vẹn: Mã hóa mật khẩu người dùng

Tính tương thích & Khả năng sử dụng: Giao diện Responsive trên Desktop/Tablet/Mobile.

