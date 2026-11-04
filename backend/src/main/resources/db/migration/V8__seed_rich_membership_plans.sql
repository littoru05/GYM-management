-- V8: Seed/update rich membership plans data with image_url, tag, and benefits

-- 1. Cap nhat hoac chen goi 1 thang (Tieu chuan / Khoi dau)
INSERT INTO memberships (id, code, name, duration_months, price, description, image_url, popular, tag, status, created_at)
VALUES (1, 'MEMBERSHIP-1M', 'Gói 1 tháng', 1, 300000.00,
        'Gói tập tiêu chuẩn trong 1 tháng, phù hợp cho người mới bắt đầu làm quen.',
        'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?q=80&w=800',
        0, 'Khởi đầu', 'ACTIVE', CURRENT_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    image_url = VALUES(image_url),
    tag = VALUES(tag),
    popular = VALUES(popular);

-- 2. Cap nhat hoac chen goi 12 thang (Tiet kiem / Uu dai lon)
INSERT INTO memberships (id, code, name, duration_months, price, description, image_url, popular, tag, status, created_at)
VALUES (2, 'MEMBERSHIP-12M', 'Gói 12 tháng', 12, 3000000.00,
        'Gói tập tiết kiệm trong 12 tháng, đặc quyền tối đa và cam kết lâu dài.',
        'https://images.unsplash.com/photo-1571902943202-507ec2618e8f?q=80&w=800',
        0, 'Tiết kiệm', 'ACTIVE', CURRENT_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    image_url = VALUES(image_url),
    tag = VALUES(tag),
    popular = VALUES(popular);

-- 3. Bo sung goi 3 thang (Standard)
INSERT INTO memberships (id, code, name, duration_months, price, description, image_url, popular, tag, status, created_at)
VALUES (3, 'MEMBERSHIP-3M', 'Gói 3 tháng', 3, 850000.00,
        'Gói tập 3 tháng linh hoạt, xây dựng thói quen tập luyện đều đặn.',
        'https://images.unsplash.com/photo-1517838277536-f5f99be501cd?q=80&w=800',
        0, 'Tiết kiệm', 'ACTIVE', CURRENT_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    image_url = VALUES(image_url),
    tag = VALUES(tag),
    popular = VALUES(popular);

-- 4. Bo sung goi 6 thang (Premium - Ban chay)
INSERT INTO memberships (id, code, name, duration_months, price, description, image_url, popular, tag, status, created_at)
VALUES (4, 'MEMBERSHIP-6M', 'Gói 6 tháng', 6, 1600000.00,
        'Gói tập 6 tháng được hội viên lựa chọn nhiều nhất, tặng kèm buổi tập cùng HLV.',
        'https://images.unsplash.com/photo-1540497077202-7c8a3999166f?q=80&w=800',
        1, 'Bán chạy', 'ACTIVE', CURRENT_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    image_url = VALUES(image_url),
    tag = VALUES(tag),
    popular = VALUES(popular);

-- Bo sung quyen loi cho goi 3 va 4 (neu chua co)
INSERT IGNORE INTO membership_benefits (membership_id, benefit) VALUES
    (3, 'Toàn bộ quyền lợi của gói 1 tháng'),
    (3, 'Tặng 1 buổi tập định hướng cùng huấn luyện viên'),
    (3, 'Tham gia các lớp Yoga & Group Class'),
    (4, 'Toàn bộ quyền lợi của gói 3 tháng'),
    (4, 'Tặng 3 buổi tập chuyên sâu cùng huấn luyện viên cá nhân'),
    (4, 'Sử dụng khu vực xông hơi đá muối & Jacuzzi'),
    (4, 'Miễn phí khăn tập & nước uống mỗi buổi tập');

