CREATE TABLE article_likes (
    article_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (article_id, user_id)
);

CREATE INDEX idx_article_likes_user
ON article_likes(user_id);