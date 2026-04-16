package com.ykw.article.service;

import com.ykw.article.dto.ArticleResponse;
import com.ykw.article.dto.CreateArticleRequest;

public interface ArticleService {

    ArticleResponse createArticle(String header, CreateArticleRequest request);

}
