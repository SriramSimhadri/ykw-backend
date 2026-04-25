package com.ykw.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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