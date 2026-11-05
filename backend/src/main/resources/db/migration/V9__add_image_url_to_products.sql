-- V9: Bổ sung cột image_url VARCHAR(500) vào bảng products và seed link ảnh tĩnh mẫu

SET @dbname = DATABASE();
SET @tablename = "products";
SET @columnname = "image_url";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  "SELECT 1",
  "ALTER TABLE products ADD COLUMN image_url VARCHAR(500) NULL AFTER cost;"
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Sao chép dữ liệu từ cột image cũ (nếu có) sang cột image_url
UPDATE products SET image_url = image WHERE image_url IS NULL AND image IS NOT NULL;

-- Cập nhật danh sách link ảnh tĩnh mẫu cho các sản phẩm hiện có
-- 1. Nước suối / Nước khoáng
UPDATE products 
SET image_url = 'https://images.unsplash.com/photo-1548839140-29a749e1bc4e?q=80&w=600'
WHERE sku LIKE '%WATER%' OR LOWER(name) LIKE '%nước suối%' OR LOWER(name) LIKE '%nước khoáng%';

-- 2. Nước tăng lực / Nước điện giải
UPDATE products 
SET image_url = 'https://images.unsplash.com/photo-1622543925917-763c34d1a86e?q=80&w=600'
WHERE sku LIKE '%ELECTRO%' OR sku LIKE '%ENERGY%' OR LOWER(name) LIKE '%tăng lực%' OR LOWER(name) LIKE '%điện giải%';

-- 3. Whey protein & Creatine
UPDATE products 
SET image_url = 'https://images.unsplash.com/photo-1593095948071-474c5cc2989d?q=80&w=600'
WHERE sku LIKE '%WHEY%' OR sku LIKE '%CREATINE%' OR LOWER(name) LIKE '%whey%' OR LOWER(name) LIKE '%creatine%';

-- 4. Bình lắc protein
UPDATE products 
SET image_url = 'https://images.unsplash.com/photo-1574680096145-d05b474e2155?q=80&w=600'
WHERE sku LIKE '%SHAKER%' OR LOWER(name) LIKE '%bình lắc%';

-- 5. Găng tay tập gym
UPDATE products 
SET image_url = 'https://images.unsplash.com/photo-1583473848882-f9a5bc7fd208?q=80&w=600'
WHERE sku LIKE '%GLOVE%' OR LOWER(name) LIKE '%găng tay%';

-- Ảnh placeholder mặc định cho các sản phẩm khác nếu chưa có ảnh
UPDATE products 
SET image_url = 'https://images.unsplash.com/photo-1517838277536-f5f99be501cd?q=80&w=600'
WHERE image_url IS NULL OR TRIM(image_url) = '';

-- Đồng bộ ngược lại cột image cũ để đảm bảo tính toàn vẹn 2 chiều
UPDATE products SET image = image_url WHERE image IS NULL OR TRIM(image) = '';

