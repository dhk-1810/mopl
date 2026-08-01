-- V1__init_image_schema.sql
CREATE TABLE timeout_images (
    id UUID PRIMARY KEY,
    image_key VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);
