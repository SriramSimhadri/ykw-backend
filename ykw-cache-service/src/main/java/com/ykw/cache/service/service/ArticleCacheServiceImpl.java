package com.ykw.cache.service.service;

import com.ykw.common.event.ArticleEvent;
import com.ykw.common.logging.LogEvent;
import com.ykw.common.logging.LogUtil;
import com.ykw.proto.Article;
import com.ykw.proto.ArticleCacheServiceGrpc;
import com.ykw.proto.GetArticleRequest;
import com.ykw.proto.GetArticleResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import static com.ykw.common.constants.Constants.ARTICLE_SLUG;

@GrpcService
@RequiredArgsConstructor
public class ArticleCacheServiceImpl extends ArticleCacheServiceGrpc.ArticleCacheServiceImplBase {

    private final RedisCacheService redisCacheService;

    @Override
    public void getArticleBySlug(GetArticleRequest request,
                                 StreamObserver<GetArticleResponse> responseObserver) {

        LogUtil.info(LogEvent.create("GET_ARTICLE_BY_SLUG_FROM_CACHE_RECEIVED")
                .add(ARTICLE_SLUG, request.getSlug()));

        String slugKey = "article:slug:" + request.getSlug();

        // Get articleId from slug
        String articleId = redisCacheService.getString(slugKey);

        if (articleId == null) {
            LogUtil.info(LogEvent.create("GET_ARTICLE_BY_SLUG_FROM_CACHE_NOT_FOUND")
                    .add(ARTICLE_SLUG, request.getSlug()));
            respondNotFound(responseObserver);
            return;
        }

        // Get article using article id
        String articleKey = "article:" + articleId;
        ArticleEvent articleObj = redisCacheService.getObject(articleKey, ArticleEvent.class);

        if (articleObj == null) {
            LogUtil.info(LogEvent.create("GET_ARTICLE_BY_SLUG_FROM_CACHE_NOT_FOUND")
                    .add(ARTICLE_SLUG, request.getSlug()));
            respondNotFound(responseObserver);
            return;
        }

        // Step 3: Convert to protobuf
        Article article = mapToProto(articleObj);

        LogUtil.info(LogEvent.create("GET_ARTICLE_BY_SLUG_FROM_CACHE_FOUND")
                .add(ARTICLE_SLUG, request.getSlug()));

        GetArticleResponse response = GetArticleResponse.newBuilder()
                .setFound(true)
                .setArticle(article)
                .build();

        LogUtil.info(LogEvent.create("GET_ARTICLE_BY_SLUG_FROM_CACHE_SUCCESS")
                .add(ARTICLE_SLUG, request.getSlug()));

        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    private void respondNotFound(StreamObserver<GetArticleResponse> observer) {
        observer.onNext(GetArticleResponse.newBuilder()
                .setFound(false)
                .build());
        observer.onCompleted();
    }

    private Article mapToProto(ArticleEvent event) {
        Article.Builder builder = Article.newBuilder()
                .setId(event.getId())
                .setSlug(event.getSlug())
                .setTitle(event.getTitle())
                .setSubtitle(event.getSubtitle())
                .setContent(event.getContent())
                .setStatus(event.getStatus())
                .setCoverImageUrl(event.getCoverImageUrl() != null ? event.getCoverImageUrl() : "");

        if (event.getPublishedAt() != null) {
            builder.setPublishedAt(event.getPublishedAt());
        }
        if (event.getCreatedAt() != null) {
            builder.setCreatedAt(event.getCreatedAt());
        }
        if (event.getUpdatedAt() != null) {
            builder.setUpdatedAt(event.getUpdatedAt());
        }

        return builder.build();
    }
}