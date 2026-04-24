DROP TABLE IF EXISTS article_tags;

DROP TABLE IF EXISTS articles;

DROP TABLE IF EXISTS tags;

CREATE TABLE articles (
    id VARCHAR(150) PRIMARY KEY,
    author_id BIGINT NOT NULL,
    slug VARCHAR(200) NOT NULL,
    title VARCHAR(150) NOT NULL,
    subtitle VARCHAR(500),
    content TEXT NOT NULL,
    cover_image_url TEXT,
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'PUBLISHED', 'DELETED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP,

    CONSTRAINT unique_slug UNIQUE (slug)
);

CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE article_tags (
    article_id VARCHAR(150) NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (article_id, tag_id),
    FOREIGN KEY (article_id)
        REFERENCES articles(id)
        ON DELETE CASCADE,
    FOREIGN KEY (tag_id)
        REFERENCES tags(id)
        ON DELETE CASCADE
);

CREATE TABLE article_idempotency (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id BIGINT,
    idempotency_key VARCHAR(255),
    article_id VARCHAR(255),
    status VARCHAR(20) NOT NULL CHECK (status IN ('IN_PROGRESS', 'COMPLETED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(author_id, idempotency_key)
);

CREATE TABLE outbox_event (
    id BIGINT PRIMARY KEY,
    event_id VARCHAR(50) NOT NULL UNIQUE,
    aggregate_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(50) NOT NULL CHECK (status IN ('ARTICLE_CREATED', 'ARTICLE_UPDATED', 'ARTICLE_DELETED')),
    payload JSONB NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('NEW', 'SENT', 'FAILED')),
    retries INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Index for outbox event selection during processing
CREATE INDEX idx_outbox_status_created
ON outbox_event(status, created_at);

-- Index for global feed
CREATE INDEX idx_articles_published
ON articles (published_at DESC)
WHERE status = 'PUBLISHED';

-- Index for author profile page
CREATE INDEX idx_articles_author_published
ON articles (author_id, published_at DESC)
WHERE status = 'PUBLISHED';

-- Tag filtering
CREATE INDEX idx_article_tags_tag
ON article_tags (tag_id, article_id);