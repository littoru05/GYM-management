-- Local/dev demo data only for Sprint 3.4 (Voucher & Staff Management).
-- Do not enable the dev profile for staging or production databases.

-- 1. Demo Staff Accounts (ACTIVE, LOCKED, INACTIVE) with BCrypt password for '123456'
INSERT INTO users
    (created_at, updated_at, code, email, last_login, name, password, phone, role, status)
SELECT CURRENT_TIMESTAMP(6), NULL, 'STF-002', 'staff.active@gym.local', NULL,
       'Trần Văn Hoạt Động', '$2a$10$3S2/QRwsJcJaAen4BTJFSOs8PEy2P1t3Ppe25sCv0lm6DwivEQXcC', '0900000003', 'STAFF', 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE code = 'STF-002' OR email = 'staff.active@gym.local'
);

INSERT INTO users
    (created_at, updated_at, code, email, last_login, name, password, phone, role, status)
SELECT CURRENT_TIMESTAMP(6), NULL, 'STF-003', 'staff.locked@gym.local', NULL,
       'Lê Thị Khóa', '$2a$10$3S2/QRwsJcJaAen4BTJFSOs8PEy2P1t3Ppe25sCv0lm6DwivEQXcC', '0900000004', 'STAFF', 'LOCKED'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE code = 'STF-003' OR email = 'staff.locked@gym.local'
);

INSERT INTO users
    (created_at, updated_at, code, email, last_login, name, password, phone, role, status)
SELECT CURRENT_TIMESTAMP(6), NULL, 'STF-004', 'staff.inactive@gym.local', NULL,
       'Nguyễn Thị Vô Hiệu Hóa', '$2a$10$3S2/QRwsJcJaAen4BTJFSOs8PEy2P1t3Ppe25sCv0lm6DwivEQXcC', '0900000005', 'STAFF', 'INACTIVE'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE code = 'STF-004' OR email = 'staff.inactive@gym.local'
);

-- 2. Demo Vouchers for testing edge cases (% cap, min order, expired, out of limit, inactive, future)
-- 2.1 Voucher giảm 20% có chặn mức giảm tối đa 50k, đơn từ 100k
INSERT INTO promotions
    (created_at, updated_at, code, name, discount_type, discount_value,
     start_date, end_date, status, usage_limit, used_count,
     min_order_amount, max_discount_amount)
SELECT CURRENT_TIMESTAMP(6), NULL, 'DISCOUNT20_CAP50K', 'Giảm 20% tối đa 50k đơn từ 100k',
       'PERCENTAGE', 20.00, '2026-01-01', '2026-12-31', 'ACTIVE', 100, 0,
       100000.00, 50000.00
WHERE NOT EXISTS (
    SELECT 1 FROM promotions WHERE code = 'DISCOUNT20_CAP50K'
);

-- 2.2 Voucher giảm tiền cố định 100k cho đơn từ 500k
INSERT INTO promotions
    (created_at, updated_at, code, name, discount_type, discount_value,
     start_date, end_date, status, usage_limit, used_count,
     min_order_amount, max_discount_amount)
SELECT CURRENT_TIMESTAMP(6), NULL, 'FIXED100K_MIN500K', 'Giảm 100k cho đơn từ 500k',
       'FIXED', 100000.00, '2026-01-01', '2026-12-31', 'ACTIVE', 100, 0,
       500000.00, NULL
WHERE NOT EXISTS (
    SELECT 1 FROM promotions WHERE code = 'FIXED100K_MIN500K'
);

-- 2.3 Voucher đã dùng hết số lượt (used_count = usage_limit)
INSERT INTO promotions
    (created_at, updated_at, code, name, discount_type, discount_value,
     start_date, end_date, status, usage_limit, used_count,
     min_order_amount, max_discount_amount)
SELECT CURRENT_TIMESTAMP(6), NULL, 'EXHAUSTED_VOUCHER', 'Voucher đã hết lượt sử dụng',
       'PERCENTAGE', 10.00, '2026-01-01', '2026-12-31', 'ACTIVE', 10, 10,
       0.00, NULL
WHERE NOT EXISTS (
    SELECT 1 FROM promotions WHERE code = 'EXHAUSTED_VOUCHER'
);

-- 2.4 Voucher đã hết hạn sử dụng (end_date trong quá khứ)
INSERT INTO promotions
    (created_at, updated_at, code, name, discount_type, discount_value,
     start_date, end_date, status, usage_limit, used_count,
     min_order_amount, max_discount_amount)
SELECT CURRENT_TIMESTAMP(6), NULL, 'EXPIRED_VOUCHER', 'Voucher đã hết hạn sử dụng',
       'PERCENTAGE', 15.00, '2025-01-01', '2025-12-31', 'ACTIVE', 50, 0,
       0.00, NULL
WHERE NOT EXISTS (
    SELECT 1 FROM promotions WHERE code = 'EXPIRED_VOUCHER'
);

-- 2.5 Voucher chưa đến ngày áp dụng (start_date trong tương lai)
INSERT INTO promotions
    (created_at, updated_at, code, name, discount_type, discount_value,
     start_date, end_date, status, usage_limit, used_count,
     min_order_amount, max_discount_amount)
SELECT CURRENT_TIMESTAMP(6), NULL, 'FUTURE_VOUCHER', 'Voucher chưa đến ngày áp dụng',
       'PERCENTAGE', 25.00, '2027-01-01', '2027-12-31', 'ACTIVE', 50, 0,
       0.00, NULL
WHERE NOT EXISTS (
    SELECT 1 FROM promotions WHERE code = 'FUTURE_VOUCHER'
);

-- 2.6 Voucher trạng thái không hoạt động (INACTIVE)
INSERT INTO promotions
    (created_at, updated_at, code, name, discount_type, discount_value,
     start_date, end_date, status, usage_limit, used_count,
     min_order_amount, max_discount_amount)
SELECT CURRENT_TIMESTAMP(6), NULL, 'INACTIVE_VOUCHER', 'Voucher đang bị tạm khóa',
       'FIXED', 30000.00, '2026-01-01', '2026-12-31', 'INACTIVE', 50, 0,
       0.00, NULL
WHERE NOT EXISTS (
    SELECT 1 FROM promotions WHERE code = 'INACTIVE_VOUCHER'
);
