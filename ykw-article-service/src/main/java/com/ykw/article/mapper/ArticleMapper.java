package com.ykw.article.mapper;

import com.ykw.article.dto.ArticleResponse;
import com.ykw.article.model.article.Article;
import com.ykw.common.logging.LogEvent;
import com.ykw.common.logging.LogUtil;
import org.mapstruct.Mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface ArticleMapper {

    ArticleResponse toResponse(Article article);

    ArticleResponse toResponse(com.ykw.proto.Article article);

    default Instant mapStringToInstant(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        try {
            return Instant.parse(source);
        } catch (Exception e) {
            LogUtil.error(LogEvent.create("EMPTY_DATE_CONVERSION_ERROR")
                    .add("ERROR", e.getMessage()));
            return null;
        }
    }

    default OffsetDateTime map(Instant instant) {
        if (instant == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
