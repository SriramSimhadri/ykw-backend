package com.ykw.article.controller;

import com.ykw.article.api.ArticlesApi;
import com.ykw.article.dto.ArticleResponse;
import com.ykw.article.dto.CreateArticleRequest;
import com.ykw.article.dto.UpdateArticleRequest;
import com.ykw.article.service.ArticleService;
import com.ykw.common.logging.LogEvent;
import com.ykw.common.logging.LogUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class ArticleController implements ArticlesApi {

    private final ArticleService articleService;

    public ResponseEntity<ArticleResponse> createArticle(String header, CreateArticleRequest request) {

        LogUtil.info(LogEvent.create("ARTICLE_CREATION_REQUEST_RECEIVED"));

        ArticleResponse articleResponse = articleService.createArticle(header, request);

        LogUtil.info(LogEvent.create("ARTICLE_CREATION_REQUEST_COMPLETED"));

        return ResponseEntity
                .created(URI.create("api/articles/" + articleResponse.getSlug()))
                .body(articleResponse);
    }

    public ResponseEntity<Void> deleteArticle(String slug) {

        LogUtil.info(LogEvent.create("ARTICLE_DELETION_REQUEST_RECEIVED"));

        articleService.deleteArticle(slug);

        LogUtil.info(LogEvent.create("ARTICLE_DELETION_REQUEST_COMPLETED"));

        return ResponseEntity.noContent()
                .build();
    }

    public ResponseEntity<ArticleResponse> updateArticle(String header, String slug, UpdateArticleRequest updateArticleRequest) {

        LogUtil.info(LogEvent.create("ARTICLE_UPDATE_REQUEST_RECEIVED"));

        ArticleResponse updated = articleService.updateArticle(header, slug, updateArticleRequest);

        LogUtil.info(LogEvent.create("ARTICLE_UPDATE_REQUEST_COMPLETED"));

        return ResponseEntity
                .created(URI.create("/api/articles/" + updated.getSlug()))
                .body(updated);

    }
}
