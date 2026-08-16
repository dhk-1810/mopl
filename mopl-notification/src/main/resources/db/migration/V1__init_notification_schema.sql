-- V1__init_notification_schema.sql
CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    receiver_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    level VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version SMALLINT NOT NULL DEFAULT 0
);

CREATE TABLE external_user_views (
    user_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    profile_image_key VARCHAR(255)
);
