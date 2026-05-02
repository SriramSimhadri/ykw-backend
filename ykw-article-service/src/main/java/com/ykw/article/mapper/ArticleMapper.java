package com.ykw.article.mapper;

import com.ykw.article.dto.ArticleResponse;
import com.ykw.article.model.article.Article;
import org.mapstruct.Mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface ArticleMapper {

    ArticleResponse toResponse(Article article);

    ArticleResponse toResponse(com.ykw.proto.Article article);

    default OffsetDateTime map(Instant instant) {
        if (instant == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
