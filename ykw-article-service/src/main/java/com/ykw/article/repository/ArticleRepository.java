package com.ykw.article.repository;

import com.ykw.article.model.article.Article;
import com.ykw.article.model.article.ArticleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, String> {

    Optional<Article> findByAuthorIdAndSlugAndStatusNot(
            Long authorId,
            String slug,
            ArticleStatus status
    );
}