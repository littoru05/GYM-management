-- V6: Add service_type to contact_leads table
ALTER TABLE contact_leads
    ADD COLUMN service_type VARCHAR(100) NULL AFTER message;

