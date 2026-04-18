package com.ykw.article.service;

import com.ykw.article.dto.ArticleResponse;
import com.ykw.article.dto.CreateArticleRequest;
import com.ykw.article.dto.UpdateArticleRequest;

public interface ArticleService {

    ArticleResponse createArticle(String header, CreateArticleRequest request);

    void deleteArticle(String slug);

    ArticleResponse updateArticle(String header, String slug, UpdateArticleRequest updateArticleRequest);
}
