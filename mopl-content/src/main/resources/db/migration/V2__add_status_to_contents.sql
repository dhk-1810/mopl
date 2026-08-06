-- V2__add_status_to_contents.sql
ALTER TABLE contents ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE';
