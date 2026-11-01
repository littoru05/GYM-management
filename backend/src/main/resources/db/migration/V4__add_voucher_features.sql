-- V4: Add voucher usage history table and enhance promotions table

-- Modify discount_type to VARCHAR(30) to support FREE, PERCENT, PERCENTAGE, FIXED
ALTER TABLE promotions
    MODIFY COLUMN discount_type VARCHAR(30) NOT NULL,
    ADD COLUMN applicable_to_all BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN applicable_membership_ids VARCHAR(255) NULL;

-- Create table to track voucher usage history
CREATE TABLE IF NOT EXISTS voucher_usages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    voucher_code VARCHAR(50) NOT NULL,
    promotion_id BIGINT NULL,
    member_id BIGINT NULL,
    membership_id BIGINT NULL,
    transaction_id BIGINT NULL,
    discount_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_voucher_usages PRIMARY KEY (id),
    CONSTRAINT fk_voucher_usages_member
        FOREIGN KEY (member_id) REFERENCES members (id) ON DELETE SET NULL,
    CONSTRAINT fk_voucher_usages_membership
        FOREIGN KEY (membership_id) REFERENCES memberships (id) ON DELETE SET NULL,
    CONSTRAINT fk_voucher_usages_transaction
        FOREIGN KEY (transaction_id) REFERENCES transactions (id) ON DELETE SET NULL,
    CONSTRAINT fk_voucher_usages_promotion
        FOREIGN KEY (promotion_id) REFERENCES promotions (id) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
