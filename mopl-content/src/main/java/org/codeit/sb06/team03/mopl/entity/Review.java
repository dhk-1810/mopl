package org.codeit.sb06.team03.mopl.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(
        name = "reviews",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_content_id_author_id",
                        columnNames = {"content_id", "author_id"}
                )
        }
)
@SQLDelete(sql = "UPDATE reviews SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Review {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @NotNull
    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Version
    @Column(name = "version", nullable = false)
    private short version;

    @NotNull
    @Column(name = "text", nullable = false)
    private String text;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "rating", nullable = false)
    private int rating;

    public static Review create(Content content, UUID authorId, String text, int rating) {
        Review review = new Review();
        review.id = UUID.randomUUID();
        review.content = content;
        review.authorId = authorId;
        review.text = text;
        review.rating = rating;
        review.createdAt = Instant.now();
        return review;
    }

    public void update(String text, Integer rating) {
        if (text != null && !text.isBlank()) {
            this.text = text;
        }
        if (rating != null) {
            this.rating = rating;
        }
    }
}
