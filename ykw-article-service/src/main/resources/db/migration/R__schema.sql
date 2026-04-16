DROP TABLE IF EXISTS article_tags;

DROP TABLE IF EXISTS articles;

DROP TABLE IF EXISTS tags;

CREATE TABLE articles (
    id VARCHAR(150) PRIMARY KEY,
    author_id BIGINT NOT NULL,
    slug VARCHAR(160) NOT NULL,
    title VARCHAR(255) NOT NULL,
    subtitle VARCHAR(500),
    content TEXT NOT NULL,
    cover_image_url TEXT,
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'PUBLISHED')),
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