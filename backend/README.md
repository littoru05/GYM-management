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

Vì vậy Flyway sẽ chạy migration schema trong `V1__init.sql`, sau đó chạy dữ liệu mẫu trong `V900__insert_demo_data.sql`.

## 5. Chạy ứng dụng

Chạy class `GymManagementApiApplication` bằng nút Run trong IntelliJ.

Khi chạy thành công, Flyway sẽ tạo các bảng nghiệp vụ và bảng:

```text
flyway_schema_history
```

Dữ liệu mẫu local/dev cũng sẽ được tạo. Tài khoản mẫu:

```text
admin@gym.local / 123456
staff@gym.local / 123456
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

Kết quả cần có migration `V1` và migration dữ liệu mẫu `V900` với `success = 1`.

## 7. Dừng database

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

## Lưu ý

- Database dùng profile `dev` nên tiếp tục được chạy bằng profile `dev`.
- Không dùng dữ liệu mẫu `V900` cho staging hoặc production.
- Không đổi `DB_PORT` sang `3306` nếu Docker Compose vẫn đang dùng port `3307`.
- Nếu port `3307` bị chiếm, cần đổi đồng thời port trong Docker Compose và biến `DB_PORT` của IntelliJ.
