-- V5: Add is_read and update status and created_at for contact_leads table
ALTER TABLE contact_leads
    ADD COLUMN is_read BOOLEAN NOT NULL DEFAULT FALSE,
    MODIFY COLUMN status VARCHAR(30) NOT NULL DEFAULT 'NEW',
    MODIFY COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);
