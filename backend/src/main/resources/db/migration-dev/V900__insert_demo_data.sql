-- Local/dev demo data only.
-- Do not enable the dev profile for staging or production databases.

INSERT INTO users
    (id, created_at, updated_at, code, email, last_login, name, password, phone, role, status)
VALUES
    (1, CURRENT_TIMESTAMP(6), NULL, 'ADM-001', 'admin@gym.local', NULL,
     'Demo Administrator', '123456', '0900000001', 'ADMIN', 'ACTIVE'),
    (2, CURRENT_TIMESTAMP(6), NULL, 'STF-001', 'staff@gym.local', NULL,
     'Demo Staff', '123456', '0900000002', 'STAFF', 'ACTIVE');

INSERT INTO categories
    (id, created_at, updated_at, code, description, name, status)
VALUES
    (1, CURRENT_TIMESTAMP(6), NULL, 'CAT-SUPPLEMENT',
     'Thực phẩm bổ sung và sản phẩm hỗ trợ tập luyện', 'Thực phẩm bổ sung', 'ACTIVE'),
    (2, CURRENT_TIMESTAMP(6), NULL, 'CAT-DRINK',
     'Nước uống và đồ uống phục vụ hội viên', 'Đồ uống', 'ACTIVE');

INSERT INTO members
    (id, created_at, updated_at, avatar, code, dob, email, join_date, name, phone, status)
VALUES
    (1, CURRENT_TIMESTAMP(6), NULL, NULL, 'MEM-0001', '1998-04-12',
     'an.nguyen@gym.local', '2026-01-10', 'Nguyễn Văn An', '0900000101', 'ACTIVE'),
    (2, CURRENT_TIMESTAMP(6), NULL, NULL, 'MEM-0002', '1996-11-25',
     'binh.tran@gym.local', '2026-02-15', 'Trần Thị Bình', '0900000102', 'EXPIRING_SOON');

INSERT INTO memberships
    (id, created_at, updated_at, code, description, duration_months, name, popular, price, status)
VALUES
    (1, CURRENT_TIMESTAMP(6), NULL, 'MEMBERSHIP-1M',
     'Gói tập tiêu chuẩn trong 1 tháng', 1, 'Gói 1 tháng', b'0', 300000.00, 'ACTIVE'),
    (2, CURRENT_TIMESTAMP(6), NULL, 'MEMBERSHIP-12M',
     'Gói tập tiết kiệm trong 12 tháng', 12, 'Gói 12 tháng', b'1', 3000000.00, 'ACTIVE');

INSERT INTO membership_benefits
    (membership_id, benefit)
VALUES
    (1, 'Sử dụng khu vực tập luyện cơ bản'),
    (1, 'Được hỗ trợ hướng dẫn tại quầy'),
    (2, 'Sử dụng toàn bộ khu vực tập luyện'),
    (2, 'Tặng 2 buổi hướng dẫn cùng huấn luyện viên'),
    (2, 'Được ưu đãi khi mua sản phẩm tại quầy');

INSERT INTO trainers
    (id, created_at, updated_at, avatar, bio, code, email, experience,
     name, phone, price_per_session, specialty, status)
VALUES
    (1, CURRENT_TIMESTAMP(6), NULL, NULL,
     'Huấn luyện viên mẫu dùng cho môi trường phát triển', 'TRN-001',
     'coach.an@gym.local', '5 năm', 'Lê Minh Anh', '0900000201',
     250000.00, 'Strength Training', 'ACTIVE'),
    (2, CURRENT_TIMESTAMP(6), NULL, NULL,
     'Huấn luyện viên mẫu dùng cho môi trường phát triển', 'TRN-002',
     'coach.huy@gym.local', '3 năm', 'Phạm Quốc Huy', '0900000202',
     200000.00, 'Cardio & Fitness', 'ACTIVE');

INSERT INTO promotions
    (id, created_at, updated_at, code, discount_type, discount_value,
     end_date, name, start_date, status, usage_limit, used_count)
VALUES
    (1, CURRENT_TIMESTAMP(6), NULL, 'WELCOME10', 'PERCENTAGE', 10.00,
     '2026-12-31', 'Ưu đãi hội viên mới', '2026-01-01', 'ACTIVE', 100, 0),
    (2, CURRENT_TIMESTAMP(6), NULL, 'SAVE50K', 'FIXED', 50000.00,
     '2026-12-31', 'Giảm 50.000 đồng', '2026-01-01', 'ACTIVE', 50, 0);

INSERT INTO products
    (id, created_at, updated_at, cost, image, min_stock, name, price,
     sku, status, stock, category_id)
VALUES
    (1, CURRENT_TIMESTAMP(6), NULL, 500000.00, NULL, 5,
     'Whey Protein Demo', 750000.00, 'SUP-WHEY-001', 'ACTIVE', 20, 1),
    (2, CURRENT_TIMESTAMP(6), NULL, 5000.00, NULL, 20,
     'Nước suối 500ml', 15000.00, 'DRK-WATER-001', 'ACTIVE', 100, 2),
    (3, CURRENT_TIMESTAMP(6), NULL, 12000.00, NULL, 10,
     'Nước điện giải', 25000.00, 'DRK-ELECTRO-001', 'ACTIVE', 60, 2);

INSERT INTO transactions
    (id, created_at, updated_at, amount, code, description, discount_amount,
     member_name, payment_method, staff_name, subtotal, transaction_time,
     type, member_id, promotion_id, staff_id)
VALUES
    (1, CURRENT_TIMESTAMP(6), NULL, 270000.00, 'TXN-0001',
     'Đăng ký gói tập demo', 30000.00, 'Nguyễn Văn An', 'CASH',
     'Demo Administrator', 300000.00, '2026-09-16 09:00:00',
     'MEMBERSHIP', 1, 1, 1),
    (2, CURRENT_TIMESTAMP(6), NULL, 30000.00, 'TXN-0002',
     'Mua sản phẩm demo', 0.00, 'Trần Thị Bình', 'CARD',
     'Demo Staff', 30000.00, '2026-09-16 10:00:00',
     'RETAIL', 2, NULL, 2);

INSERT INTO member_subscriptions
    (id, created_at, updated_at, code, end_date, notes, paid_price,
     start_date, status, member_id, membership_id, transaction_id)
VALUES
    (1, CURRENT_TIMESTAMP(6), NULL, 'SUB-0001', '2026-10-15',
     'Subscription demo', 270000.00, '2026-09-16', 'ACTIVE', 1, 1, 1);

INSERT INTO transaction_details
    (id, created_at, updated_at, item_name, item_type, quantity,
     total_price, unit_cost, unit_price, membership_id, product_id,
     trainer_id, transaction_id)
VALUES
    (1, CURRENT_TIMESTAMP(6), NULL, 'Gói 1 tháng', 'MEMBERSHIP', 1,
     270000.00, NULL, 270000.00, 1, NULL, NULL, 1),
    (2, CURRENT_TIMESTAMP(6), NULL, 'Nước suối 500ml', 'PRODUCT', 2,
     30000.00, 5000.00, 15000.00, NULL, 2, NULL, 2);

INSERT INTO check_ins
    (id, created_at, updated_at, avatar, check_in_time, check_out_time,
     code, member_name, membership_name, reason, status, member_id, subscription_id)
VALUES
    (1, CURRENT_TIMESTAMP(6), NULL, NULL, '2026-09-16 08:00:00', NULL,
     'CHECKIN-0001', 'Nguyễn Văn An', 'Gói 1 tháng', NULL,
     'SUCCESS', 1, 1);

INSERT INTO contact_leads
    (id, created_at, updated_at, assigned_staff_name, email, message,
     name, notes, phone, status, assigned_staff_id)
VALUES
    (1, CURRENT_TIMESTAMP(6), NULL, 'Demo Staff', 'lead@gym.local',
     'Tôi muốn được tư vấn gói tập 12 tháng.', 'Demo Lead',
     'Lead mẫu cho màn hình quản lý tư vấn.', '0900000301',
     'PENDING', 2);

INSERT INTO notifications
    (id, created_at, updated_at, action_url, message, read_status,
     target_role, title, type, staff_id)
VALUES
    (1, CURRENT_TIMESTAMP(6), NULL, '/members',
     'Có hội viên mới cần được kiểm tra thông tin.', b'0',
     'STAFF', 'Hội viên mới', 'MEMBER', 2),
    (2, CURRENT_TIMESTAMP(6), NULL, '/transactions',
     'Giao dịch demo đã được ghi nhận.', b'0',
     'ALL', 'Giao dịch thành công', 'SUCCESS', 1);
