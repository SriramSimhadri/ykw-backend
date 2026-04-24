package com.ykw.article.model.idempotency;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "article_idempotency",
        indexes = {
                @Index(name = "idx_author_key", columnList = "author_id, idempotency_key")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_author_idempotency",
                        columnNames = {"author_id", "idempotency_key"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleIdempotency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "article_id")
    private String articleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CreationStatus creationStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public boolean isCompleted() {
        return CreationStatus.COMPLETED.equals(this.creationStatus);
    }

    public boolean isInProgress() {
        return CreationStatus.IN_PROGRESS.equals(this.creationStatus);
    }
}