CREATE TABLE categories
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NULL,
    code        VARCHAR(50)  NOT NULL,
    description VARCHAR(255) NULL,
    name        VARCHAR(100) NOT NULL,
    status      ENUM ('ACTIVE', 'INACTIVE') NOT NULL,
    CONSTRAINT pk_categories PRIMARY KEY (id),
    CONSTRAINT uk_categories_code UNIQUE (code),
    CONSTRAINT uk_categories_name UNIQUE (name)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE users
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NULL,
    code       VARCHAR(50)  NOT NULL,
    email      VARCHAR(100) NOT NULL,
    last_login DATETIME(6)  NULL,
    name       VARCHAR(100) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    phone      VARCHAR(20)  NULL,
    role       ENUM ('ADMIN', 'STAFF') NOT NULL,
    status     ENUM ('ACTIVE', 'INACTIVE', 'LOCKED') NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_code UNIQUE (code),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE members
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NULL,
    avatar     VARCHAR(500) NULL,
    code       VARCHAR(50)  NOT NULL,
    dob        DATE         NULL,
    email      VARCHAR(100) NULL,
    join_date  DATE         NOT NULL,
    name       VARCHAR(100) NOT NULL,
    phone      VARCHAR(20)  NOT NULL,
    status     ENUM ('ACTIVE', 'EXPIRED', 'EXPIRING_SOON', 'LOCKED') NOT NULL,
    CONSTRAINT pk_members PRIMARY KEY (id),
    CONSTRAINT uk_members_code UNIQUE (code),
    CONSTRAINT uk_members_phone UNIQUE (phone)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE memberships
(
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    created_at      DATETIME(6)    NOT NULL,
    updated_at      DATETIME(6)    NULL,
    code            VARCHAR(50)    NOT NULL,
    description     TEXT           NULL,
    duration_months INT            NOT NULL,
    name            VARCHAR(100)   NOT NULL,
    popular         BIT(1)         NULL,
    price           DECIMAL(15, 2) NOT NULL,
    status          ENUM ('ACTIVE', 'INACTIVE') NOT NULL,
    CONSTRAINT pk_memberships PRIMARY KEY (id),
    CONSTRAINT uk_memberships_code UNIQUE (code),
    CONSTRAINT uk_memberships_name UNIQUE (name)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE trainers
(
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    created_at        DATETIME(6)    NOT NULL,
    updated_at        DATETIME(6)    NULL,
    avatar            VARCHAR(500)   NULL,
    bio               VARCHAR(500)   NULL,
    code              VARCHAR(50)    NOT NULL,
    email             VARCHAR(100)   NULL,
    experience        VARCHAR(100)   NULL,
    name              VARCHAR(100)   NOT NULL,
    phone             VARCHAR(20)    NULL,
    price_per_session DECIMAL(15, 2) NULL,
    specialty         VARCHAR(150)   NULL,
    status            ENUM ('ACTIVE', 'INACTIVE') NOT NULL,
    CONSTRAINT pk_trainers PRIMARY KEY (id),
    CONSTRAINT uk_trainers_code UNIQUE (code)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE promotions
(
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    created_at     DATETIME(6)    NOT NULL,
    updated_at     DATETIME(6)    NULL,
    code           VARCHAR(50)    NOT NULL,
    discount_type  ENUM ('FIXED', 'PERCENTAGE') NOT NULL,
    discount_value DECIMAL(15, 2) NOT NULL,
    end_date       DATE           NOT NULL,
    name           VARCHAR(150)   NOT NULL,
    start_date     DATE           NOT NULL,
    status         ENUM ('ACTIVE', 'INACTIVE') NOT NULL,
    usage_limit    INT            NOT NULL,
    used_count     INT            NOT NULL,
    CONSTRAINT pk_promotions PRIMARY KEY (id),
    CONSTRAINT uk_promotions_code UNIQUE (code)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE membership_benefits
(
    membership_id BIGINT       NOT NULL,
    benefit       VARCHAR(255) NULL,
    CONSTRAINT fk_membership_benefits_membership
        FOREIGN KEY (membership_id) REFERENCES memberships (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE products
(
    id          BIGINT         NOT NULL AUTO_INCREMENT,
    created_at  DATETIME(6)    NOT NULL,
    updated_at  DATETIME(6)    NULL,
    cost        DECIMAL(15, 2) NOT NULL,
    image       VARCHAR(500)   NULL,
    min_stock   INT            NOT NULL,
    name        VARCHAR(150)   NOT NULL,
    price       DECIMAL(15, 2) NOT NULL,
    sku         VARCHAR(50)    NOT NULL,
    status      ENUM ('ACTIVE', 'INACTIVE') NOT NULL,
    stock       INT            NOT NULL,
    category_id BIGINT         NOT NULL,
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT uk_products_sku UNIQUE (sku),
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE transactions
(
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    created_at       DATETIME(6)    NOT NULL,
    updated_at       DATETIME(6)    NULL,
    amount           DECIMAL(15, 2) NOT NULL,
    code             VARCHAR(50)    NOT NULL,
    description      VARCHAR(255)   NULL,
    discount_amount  DECIMAL(15, 2) NULL,
    member_name      VARCHAR(100)   NULL,
    payment_method   ENUM ('BANK_TRANSFER', 'CARD', 'CASH', 'E_WALLET') NOT NULL,
    staff_name       VARCHAR(100)   NULL,
    subtotal         DECIMAL(15, 2) NULL,
    transaction_time DATETIME(6)    NOT NULL,
    type             ENUM ('MEMBERSHIP', 'RETAIL') NOT NULL,
    member_id        BIGINT         NULL,
    promotion_id     BIGINT         NULL,
    staff_id         BIGINT         NULL,
    CONSTRAINT pk_transactions PRIMARY KEY (id),
    CONSTRAINT uk_transactions_code UNIQUE (code),
    CONSTRAINT fk_transactions_member
        FOREIGN KEY (member_id) REFERENCES members (id),
    CONSTRAINT fk_transactions_promotion
        FOREIGN KEY (promotion_id) REFERENCES promotions (id),
    CONSTRAINT fk_transactions_staff
        FOREIGN KEY (staff_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE member_subscriptions
(
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    created_at     DATETIME(6)    NOT NULL,
    updated_at     DATETIME(6)    NULL,
    code           VARCHAR(50)    NOT NULL,
    end_date       DATE           NOT NULL,
    notes          VARCHAR(255)   NULL,
    paid_price     DECIMAL(15, 2) NOT NULL,
    start_date     DATE           NOT NULL,
    status         ENUM ('ACTIVE', 'CANCELLED', 'EXPIRED', 'PENDING') NOT NULL,
    member_id      BIGINT         NOT NULL,
    membership_id  BIGINT         NOT NULL,
    transaction_id  BIGINT        NULL,
    CONSTRAINT pk_member_subscriptions PRIMARY KEY (id),
    CONSTRAINT uk_member_subscriptions_code UNIQUE (code),
    CONSTRAINT fk_member_subscriptions_member
        FOREIGN KEY (member_id) REFERENCES members (id),
    CONSTRAINT fk_member_subscriptions_membership
        FOREIGN KEY (membership_id) REFERENCES memberships (id),
    CONSTRAINT fk_member_subscriptions_transaction
        FOREIGN KEY (transaction_id) REFERENCES transactions (id)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE transaction_details
(
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    created_at      DATETIME(6)    NOT NULL,
    updated_at      DATETIME(6)    NULL,
    item_name       VARCHAR(150)   NOT NULL,
    item_type       VARCHAR(30)    NOT NULL,
    quantity        INT            NOT NULL,
    total_price     DECIMAL(15, 2) NOT NULL,
    unit_cost       DECIMAL(15, 2) NULL,
    unit_price      DECIMAL(15, 2) NOT NULL,
    membership_id   BIGINT         NULL,
    product_id      BIGINT         NULL,
    trainer_id      BIGINT         NULL,
    transaction_id  BIGINT         NOT NULL,
    CONSTRAINT pk_transaction_details PRIMARY KEY (id),
    CONSTRAINT fk_transaction_details_membership
        FOREIGN KEY (membership_id) REFERENCES memberships (id),
    CONSTRAINT fk_transaction_details_product
        FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_transaction_details_trainer
        FOREIGN KEY (trainer_id) REFERENCES trainers (id),
    CONSTRAINT fk_transaction_details_transaction
        FOREIGN KEY (transaction_id) REFERENCES transactions (id)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE check_ins
(
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    created_at      DATETIME(6)   NOT NULL,
    updated_at      DATETIME(6)   NULL,
    avatar          VARCHAR(500)  NULL,
    check_in_time   DATETIME(6)   NOT NULL,
    check_out_time  DATETIME(6)   NULL,
    code            VARCHAR(50)   NOT NULL,
    member_name     VARCHAR(100)  NOT NULL,
    membership_name VARCHAR(100)  NULL,
    reason          VARCHAR(255)  NULL,
    status          ENUM ('DENIED', 'SUCCESS') NOT NULL,
    member_id       BIGINT        NOT NULL,
    subscription_id BIGINT        NULL,
    CONSTRAINT pk_check_ins PRIMARY KEY (id),
    CONSTRAINT uk_check_ins_code UNIQUE (code),
    CONSTRAINT fk_check_ins_member
        FOREIGN KEY (member_id) REFERENCES members (id),
    CONSTRAINT fk_check_ins_subscription
        FOREIGN KEY (subscription_id) REFERENCES member_subscriptions (id)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE contact_leads
(
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    created_at          DATETIME(6)   NOT NULL,
    updated_at          DATETIME(6)   NULL,
    assigned_staff_name VARCHAR(100)  NULL,
    email               VARCHAR(100)  NULL,
    message             TEXT          NULL,
    name                VARCHAR(100)  NOT NULL,
    notes               VARCHAR(500)  NULL,
    phone               VARCHAR(20)   NOT NULL,
    status              ENUM ('CANCELLED', 'CONTACTED', 'CONVERTED', 'PENDING') NOT NULL,
    assigned_staff_id   BIGINT        NULL,
    CONSTRAINT pk_contact_leads PRIMARY KEY (id),
    CONSTRAINT fk_contact_leads_staff
        FOREIGN KEY (assigned_staff_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE notifications
(
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    created_at  DATETIME(6)   NOT NULL,
    updated_at  DATETIME(6)   NULL,
    action_url  VARCHAR(255)  NULL,
    message     TEXT          NOT NULL,
    read_status BIT(1)        NOT NULL,
    target_role VARCHAR(20)   NULL,
    title       VARCHAR(150)  NOT NULL,
    type        ENUM ('CHECK_IN', 'INVENTORY', 'MEMBER', 'SUCCESS', 'SYSTEM', 'TRANSACTION', 'WARNING') NOT NULL,
    staff_id    BIGINT        NULL,
    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT fk_notifications_staff
        FOREIGN KEY (staff_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
