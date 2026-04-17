package com.ykw.article.repository;

import com.ykw.article.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ArticleRepository extends JpaRepository<Article, String> {

    @Modifying
    @Query("""
    UPDATE Article a
    SET a.status = 'DELETED'
    WHERE a.slug = :slug AND a.authorId = :authorId""")
    long softDeleteBySlugAndAuthorId(String slug, Long authorId);
}
