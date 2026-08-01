-- V1__init_dm_schema.sql
CREATE TABLE dm_chat_rooms (
    id UUID PRIMARY KEY,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    user1_id UUID NOT NULL,
    user2_id UUID NOT NULL
);

CREATE TABLE dm_messages (
    id UUID PRIMARY KEY,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    chat_room_id UUID NOT NULL REFERENCES dm_chat_rooms(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL,
    message TEXT NOT NULL
);

CREATE TABLE dm_chat_room_stats (
    chat_room_id UUID PRIMARY KEY REFERENCES dm_chat_rooms(id) ON DELETE CASCADE,
    last_message_id UUID,
    last_message_time TIMESTAMP WITH TIME ZONE,
    unread_count_user1 INT NOT NULL DEFAULT 0,
    unread_count_user2 INT NOT NULL DEFAULT 0
);

CREATE TABLE external_user_views (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    image_key VARCHAR(255)
);

CREATE TABLE external_image_views (
    image_key VARCHAR(255) PRIMARY KEY,
    url TEXT
);

-- Indexes
CREATE INDEX idx_dm_chat_rooms_users ON dm_chat_rooms(user1_id, user2_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_dm_messages_room_created ON dm_messages(chat_room_id, created_at DESC) WHERE is_deleted = FALSE;
