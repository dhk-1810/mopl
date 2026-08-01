-- V1__init_playlist_schema.sql
CREATE TABLE curations (
    id UUID PRIMARY KEY,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version SMALLINT NOT NULL DEFAULT 0,
    user_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE playlists (
    id UUID PRIMARY KEY,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version SMALLINT NOT NULL DEFAULT 0,
    curation_id UUID NOT NULL REFERENCES curations(id) ON DELETE CASCADE,
    content_id UUID NOT NULL,
    sequence INT NOT NULL
);

CREATE TABLE subscriptions (
    id UUID PRIMARY KEY,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version SMALLINT NOT NULL DEFAULT 0,
    subscriber_id UUID NOT NULL,
    curation_id UUID NOT NULL REFERENCES curations(id) ON DELETE CASCADE
);

CREATE TABLE external_user_views (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    image_key VARCHAR(255)
);

CREATE TABLE external_content_views (
    id UUID PRIMARY KEY,
    title VARCHAR(255),
    type VARCHAR(50),
    thumbnail_key VARCHAR(255)
);

CREATE TABLE external_follow_views (
    id UUID PRIMARY KEY,
    followee_id UUID NOT NULL,
    follower_id UUID NOT NULL
);

CREATE TABLE external_image_views (
    image_key VARCHAR(255) PRIMARY KEY,
    url TEXT
);

-- Indexes
CREATE INDEX idx_curations_user_id ON curations(user_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_playlists_curation_seq ON playlists(curation_id, sequence) WHERE is_deleted = FALSE;
CREATE INDEX idx_subscriptions_subscriber ON subscriptions(subscriber_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_subscriptions_curation ON subscriptions(curation_id) WHERE is_deleted = FALSE;
