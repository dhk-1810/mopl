-- V1__init_dm_schema.sql
CREATE TABLE dm_chat_rooms (
    id UUID PRIMARY KEY,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version SMALLINT NOT NULL DEFAULT 0,
    user1_id UUID NOT NULL,
    user2_id UUID NOT NULL
);

CREATE TABLE dm_messages (
    id UUID PRIMARY KEY,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    dm_chat_room_id UUID NOT NULL REFERENCES dm_chat_rooms(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL,
    receiver_id UUID NOT NULL,
    content VARCHAR(1000) NOT NULL,
    has_unread BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE dm_chat_room_stats (
    id UUID PRIMARY KEY,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    dm_chat_room_id UUID NOT NULL REFERENCES dm_chat_rooms(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version SMALLINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    account_id UUID NOT NULL,
    activity BOOLEAN NOT NULL DEFAULT FALSE,
    has_unread BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_dm_chat_room_stats_room_account UNIQUE (dm_chat_room_id, account_id)
);

CREATE TABLE external_user_views (
    user_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    profile_image_key VARCHAR(255)
);

CREATE TABLE timeout_images (
    id UUID PRIMARY KEY,
    image_key VARCHAR(255) UNIQUE,
    presigned_url VARCHAR(1024),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    exp TIMESTAMP WITH TIME ZONE
);

-- Indexes
CREATE INDEX idx_dm_chat_rooms_users ON dm_chat_rooms(user1_id, user2_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_dm_messages_room_created ON dm_messages(dm_chat_room_id, created_at DESC) WHERE is_deleted = FALSE;
