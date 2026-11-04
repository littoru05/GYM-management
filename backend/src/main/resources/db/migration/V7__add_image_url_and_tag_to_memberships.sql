-- V7: Add image_url and tag columns to memberships table
ALTER TABLE memberships
    ADD COLUMN image_url VARCHAR(500) NULL AFTER description,
    ADD COLUMN tag VARCHAR(50) NULL AFTER popular;

