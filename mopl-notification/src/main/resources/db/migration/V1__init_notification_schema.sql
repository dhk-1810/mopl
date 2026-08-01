-- V1__init_notification_schema.sql
CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    receiver_id UUID NOT NULL,
    sender_id UUID,
    type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    target_id UUID
);

CREATE TABLE external_user_views (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    image_key VARCHAR(255)
);
