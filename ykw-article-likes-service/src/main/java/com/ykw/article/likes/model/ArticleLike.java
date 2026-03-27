package com.ykw.article.likes.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "article_likes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleLike {

    @EmbeddedId
    private ArticleLikeId id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}