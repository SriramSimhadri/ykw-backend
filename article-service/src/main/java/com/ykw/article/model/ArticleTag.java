package com.ykw.article.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "article_tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleTag {

    @EmbeddedId
    private ArticleTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("articleId")
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
}