-- V1__init_content_schema.sql
CREATE TABLE contents (
    id UUID PRIMARY KEY,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version SMALLINT NOT NULL DEFAULT 0,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(10000) NOT NULL,
    thumbnail_key VARCHAR(255) NOT NULL,
    average_rating DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    review_count BIGINT NOT NULL DEFAULT 0,
    watcher_count BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    content_id UUID NOT NULL REFERENCES contents(id) ON DELETE CASCADE,
    author_id UUID NOT NULL,
    version SMALLINT NOT NULL DEFAULT 0,
    text TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    rating INT NOT NULL,
    CONSTRAINT uq_content_id_author_id UNIQUE (content_id, author_id)
);

CREATE TABLE tags (
    id UUID PRIMARY KEY,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE contents_tags (
    content_id UUID NOT NULL REFERENCES contents(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (content_id, tag_id)
);

CREATE TABLE live_chat_rooms (
    content_id UUID PRIMARY KEY REFERENCES contents(id) ON DELETE CASCADE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version SMALLINT NOT NULL DEFAULT 0
);

CREATE TABLE review_stats (
    content_id UUID PRIMARY KEY REFERENCES contents(id) ON DELETE CASCADE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    rating_sum BIGINT NOT NULL DEFAULT 0,
    review_count INT NOT NULL DEFAULT 0,
    version SMALLINT NOT NULL DEFAULT 0
);

CREATE TABLE timeout_images (
    id UUID PRIMARY KEY,
    image_key VARCHAR(255) UNIQUE,
    presigned_url VARCHAR(1024),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    exp TIMESTAMP WITH TIME ZONE
);

-- Extensions & Special Indexes
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_contents_name_lower_trgm
    ON contents USING gin (LOWER(title) gin_trgm_ops);

-- Standard Indexes
CREATE INDEX idx_contents_type_created ON contents(type, created_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_contents_title ON contents(title) WHERE is_deleted = FALSE;
CREATE INDEX idx_reviews_content_created ON reviews(content_id, created_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_reviews_author_id ON reviews(author_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_contents_tags_tag_id ON contents_tags(tag_id);
