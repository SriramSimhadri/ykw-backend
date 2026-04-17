package com.ykw.article.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleTagId implements Serializable {

    @Column(name = "article_id", nullable = false)
    private String articleId;

    @Column(name = "tag_id", nullable = false)
    private Long tagId;
}