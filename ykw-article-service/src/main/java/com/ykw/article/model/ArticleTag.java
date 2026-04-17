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

    @Column(name = "article_id", nullable = false, insertable = false, updatable = false)
    private String articleId;

    @Column(name = "tag_id", nullable = false, insertable = false, updatable = false)
    private Long tagId;
}