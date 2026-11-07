-- Inventory/POS demo data for product listing, stock alerts, and sale validation.

INSERT INTO categories
    (created_at, updated_at, code, description, name, status)
SELECT CURRENT_TIMESTAMP(6), NULL, 'CAT-SUPPLEMENT',
       'Whey, creatine và các sản phẩm hỗ trợ tập luyện',
       'Thực phẩm bổ sung', 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1 FROM categories
    WHERE code = 'CAT-SUPPLEMENT' OR name = 'Thực phẩm bổ sung'
);

INSERT INTO categories
    (created_at, updated_at, code, description, name, status)
SELECT CURRENT_TIMESTAMP(6), NULL, 'CAT-BEVERAGE',
       'Nước giải khát và đồ uống thể thao',
       'Nước giải khát', 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1 FROM categories
    WHERE code = 'CAT-BEVERAGE' OR name = 'Nước giải khát'
);

INSERT INTO categories
    (created_at, updated_at, code, description, name, status)
SELECT CURRENT_TIMESTAMP(6), NULL, 'CAT-ACCESSORY',
       'Phụ kiện phục vụ tập luyện tại phòng gym',
       'Phụ kiện tập', 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1 FROM categories
    WHERE code = 'CAT-ACCESSORY' OR name = 'Phụ kiện tập'
);

INSERT IGNORE INTO products
    (created_at, updated_at, cost, image, image_url, min_stock, name, price,
     sku, status, stock, category_id)
VALUES
    (CURRENT_TIMESTAMP(6), NULL, 820000.00, 'https://images.unsplash.com/photo-1593095948071-474c5cc2989d?q=80&w=600', 'https://images.unsplash.com/photo-1593095948071-474c5cc2989d?q=80&w=600', 10,
     'Whey Protein Vanilla 1kg', 1250000.00, 'SUP-WHEY-VAN-001',
     'ACTIVE', 72, (SELECT id FROM categories WHERE name = 'Thực phẩm bổ sung')),
    (CURRENT_TIMESTAMP(6), NULL, 420000.00, 'https://images.unsplash.com/photo-1593095948071-474c5cc2989d?q=80&w=600', 'https://images.unsplash.com/photo-1593095948071-474c5cc2989d?q=80&w=600', 8,
     'Creatine Monohydrate 300g', 650000.00, 'SUP-CREATINE-300-001',
     'ACTIVE', 54, (SELECT id FROM categories WHERE name = 'Thực phẩm bổ sung')),
    (CURRENT_TIMESTAMP(6), NULL, 5000.00, 'https://images.unsplash.com/photo-1548839140-29a749e1bc4e?q=80&w=600', 'https://images.unsplash.com/photo-1548839140-29a749e1bc4e?q=80&w=600', 20,
     'Nước suối khoáng Lavie 500ml', 10000.00, 'DRK-WATER-LAVIE-500',
     'ACTIVE', 120, (SELECT id FROM categories WHERE name = 'Nước giải khát')),
    (CURRENT_TIMESTAMP(6), NULL, 12000.00, 'https://images.unsplash.com/photo-1622543925917-763c34d1a86e?q=80&w=600', 'https://images.unsplash.com/photo-1622543925917-763c34d1a86e?q=80&w=600', 15,
     'Nước tăng lực bò húc RedBull 250ml', 20000.00, 'DRK-ENERGY-REDBULL-001',
     'ACTIVE', 80, (SELECT id FROM categories WHERE name = 'Nước giải khát')),
    (CURRENT_TIMESTAMP(6), NULL, 11000.00, 'https://images.unsplash.com/photo-1622543925917-763c34d1a86e?q=80&w=600', 'https://images.unsplash.com/photo-1622543925917-763c34d1a86e?q=80&w=600', 20,
     'Nước điện giải vị cam 500ml', 22000.00, 'DRK-ELECTRO-ORANGE-001',
     'ACTIVE', 100, (SELECT id FROM categories WHERE name = 'Nước giải khát')),
    (CURRENT_TIMESTAMP(6), NULL, 200000.00, 'https://images.unsplash.com/photo-1583473848882-f9a5bc7fd208?q=80&w=600', 'https://images.unsplash.com/photo-1583473848882-f9a5bc7fd208?q=80&w=600', 10,
     'Găng tay tập gym', 350000.00, 'ACC-GLOVE-GYM-001',
     'ACTIVE', 50, (SELECT id FROM categories WHERE name = 'Phụ kiện tập')),
    (CURRENT_TIMESTAMP(6), NULL, 90000.00, 'https://images.unsplash.com/photo-1574680096145-d05b474e2155?q=80&w=600', 'https://images.unsplash.com/photo-1574680096145-d05b474e2155?q=80&w=600', 12,
     'Bình lắc protein 700ml', 160000.00, 'ACC-SHAKER-700-001',
     'ACTIVE', 12, (SELECT id FROM categories WHERE name = 'Phụ kiện tập')),
    (CURRENT_TIMESTAMP(6), NULL, 810000.00, 'https://images.unsplash.com/photo-1593095948071-474c5cc2989d?q=80&w=600', 'https://images.unsplash.com/photo-1593095948071-474c5cc2989d?q=80&w=600', 10,
     'Whey Protein Chocolate 1kg', 1240000.00, 'SUP-WHEY-CHOCO-001',
     'ACTIVE', 4, (SELECT id FROM categories WHERE name = 'Thực phẩm bổ sung')),
    (CURRENT_TIMESTAMP(6), NULL, 150000.00, 'https://images.unsplash.com/photo-1593095948071-474c5cc2989d?q=80&w=600', 'https://images.unsplash.com/photo-1593095948071-474c5cc2989d?q=80&w=600', 5,
     'Creatine Monohydrate 100g', 250000.00, 'SUP-CREATINE-100-001',
     'ACTIVE', 2, (SELECT id FROM categories WHERE name = 'Thực phẩm bổ sung')),
    (CURRENT_TIMESTAMP(6), NULL, 10000.00, 'https://images.unsplash.com/photo-1622543925917-763c34d1a86e?q=80&w=600', 'https://images.unsplash.com/photo-1622543925917-763c34d1a86e?q=80&w=600', 15,
     'Nước điện giải vị chanh 500ml', 22000.00, 'DRK-ELECTRO-LEMON-001',
     'ACTIVE', 1, (SELECT id FROM categories WHERE name = 'Nước giải khát')),
    (CURRENT_TIMESTAMP(6), NULL, 70000.00, 'https://images.unsplash.com/photo-1598289431512-b97b0917affc?q=80&w=600', 'https://images.unsplash.com/photo-1598289431512-b97b0917affc?q=80&w=600', 5,
     'Dây kháng lực', 130000.00, 'ACC-RESISTANCE-BAND-001',
     'ACTIVE', 0, (SELECT id FROM categories WHERE name = 'Phụ kiện tập')),
    (CURRENT_TIMESTAMP(6), NULL, 300000.00, 'https://images.unsplash.com/photo-1598289431512-b97b0917affc?q=80&w=600', 'https://images.unsplash.com/photo-1598289431512-b97b0917affc?q=80&w=600', 5,
     'Dây nhảy tốc độ', 480000.00, 'ACC-SPEED-ROPE-001',
     'ACTIVE', 18, (SELECT id FROM categories WHERE name = 'Phụ kiện tập'));
