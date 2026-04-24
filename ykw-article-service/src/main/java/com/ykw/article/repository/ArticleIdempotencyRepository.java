package com.ykw.article.repository;

import com.ykw.article.model.idempotency.ArticleIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface ArticleIdempotencyRepository extends JpaRepository<ArticleIdempotency, Long> {

    Optional<ArticleIdempotency> findByAuthorIdAndIdempotencyKey(Long authorId, String idempotencyKey);

    @Modifying
    @Query("""
    DELETE FROM ArticleIdempotency i
    WHERE
        (i.creationStatus = 'COMPLETED'
            AND i.createdAt < :cutoffCompleted)
        OR
        (i.creationStatus = 'IN_PROGRESS'
            AND i.createdAt < :cutoffInProgress)
""")
    int deleteOldRecords(Instant cutoffCompleted, Instant cutoffInProgress);
}