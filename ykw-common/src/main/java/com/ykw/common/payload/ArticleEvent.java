package com.ykw.common.payload;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ArticleEvent {

    private String id;

    private Long authorId;

    private String slug;

    private String title;

    private String subtitle;

    private String content;

    private String coverImageUrl;

    private String status;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant publishedAt;

}