package com.ykw.article.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArticleEventPayload {
    private String articleId;
    private String slug;
    private Long authorId;
    private String title;
    private String status;
}