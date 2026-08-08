-- V1__init_user_schema.sql
CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version SMALLINT NOT NULL DEFAULT 0,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    locked BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE profiles (
    id UUID PRIMARY KEY REFERENCES accounts(id) ON DELETE CASCADE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version SMALLINT NOT NULL DEFAULT 0,
    name VARCHAR(255) NOT NULL,
    image_key VARCHAR(255)
);

CREATE TABLE followees (
    id UUID PRIMARY KEY,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    follower_count BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE followers (
    followee_id UUID NOT NULL REFERENCES followees(id) ON DELETE CASCADE,
    follower_id UUID NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (followee_id, follower_id)
);

-- Indexes
CREATE INDEX idx_accounts_email ON accounts(email) WHERE is_deleted = FALSE;
CREATE INDEX idx_followers_follower_id ON followers(follower_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_profiles_name ON profiles(name) WHERE is_deleted = FALSE;
