-- V1__init_content_schema.sql
CREATE TABLE contents (
    id UUID PRIMARY KEY,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
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
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE content_tags (
    content_id UUID NOT NULL REFERENCES contents(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (content_id, tag_id)
);

CREATE TABLE live_chat_rooms (
    id UUID PRIMARY KEY,
    content_id UUID NOT NULL REFERENCES contents(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE review_stats (
    content_id UUID PRIMARY KEY REFERENCES contents(id) ON DELETE CASCADE,
    review_count BIGINT NOT NULL DEFAULT 0,
    average_rating DOUBLE PRECISION NOT NULL DEFAULT 0.0
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
CREATE INDEX idx_contents_type_created ON contents(type, created_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_contents_title ON contents(title) WHERE is_deleted = FALSE;
CREATE INDEX idx_reviews_content_created ON reviews(content_id, created_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_reviews_author_id ON reviews(author_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_content_tags_tag_id ON content_tags(tag_id);
