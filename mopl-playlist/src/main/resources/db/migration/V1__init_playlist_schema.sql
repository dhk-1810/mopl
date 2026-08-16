-- V1__init_playlist_schema.sql
CREATE TABLE playlists (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version SMALLINT NOT NULL DEFAULT 0,
    subscriber_count BIGINT NOT NULL DEFAULT 0,
    content_count BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE curations (
    playlist_id UUID NOT NULL,
    content_id UUID NOT NULL,
    content_title VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (playlist_id, content_id)
);

CREATE TABLE subscriptions (
    playlist_id UUID NOT NULL,
    subscriber_id UUID NOT NULL,
    PRIMARY KEY (playlist_id, subscriber_id)
);

CREATE TABLE external_user_views (
    user_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    profile_image_key VARCHAR(255)
);

CREATE TABLE external_content_views (
    content_id UUID PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    thumbnail_key VARCHAR(255),
    tags TEXT,
    average_rating DOUBLE PRECISION NOT NULL DEFAULT 0,
    review_count BIGINT NOT NULL DEFAULT 0,
    watcher_count BIGINT NOT NULL DEFAULT 0
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

CREATE INDEX idx_playlists_updated_at ON playlists(updated_at, id);
