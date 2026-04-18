package com.ykw.article.repository;

import com.ykw.article.model.ArticleIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleIdempotencyRepository extends JpaRepository<ArticleIdempotency, Long> {

    Optional<ArticleIdempotency> findByAuthorIdAndIdempotencyKey(Long authorId, String idempotencyKey);
}