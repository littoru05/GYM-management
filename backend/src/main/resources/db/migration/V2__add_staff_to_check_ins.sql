ALTER TABLE check_ins
    ADD COLUMN staff_id BIGINT NULL,
    ADD COLUMN staff_name VARCHAR(100) NULL,
    ADD CONSTRAINT fk_check_ins_staff
        FOREIGN KEY (staff_id) REFERENCES users (id);
