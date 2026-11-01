ALTER TABLE promotions
    ADD COLUMN min_order_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    ADD COLUMN max_discount_amount DECIMAL(15, 2) NULL;
