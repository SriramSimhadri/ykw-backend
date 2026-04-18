package com.ykw.article.repository;

import com.ykw.article.model.Article;
import com.ykw.article.model.ArticleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, String> {

    @Modifying
    @Query("""
        UPDATE Article a
        SET a.status = com.ykw.article.model.ArticleStatus.DELETED
        WHERE a.slug = :slug AND a.authorId = :authorId
    """)
    long softDeleteBySlugAndAuthorId(String slug, Long authorId);

    Optional<Article> findByAuthorIdAndSlugAndStatusNot(
            Long authorId,
            String slug,
            ArticleStatus status
    );

    Optional<Article> findById(String id);
}