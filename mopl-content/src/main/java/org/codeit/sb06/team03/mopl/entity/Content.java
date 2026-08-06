package org.codeit.sb06.team03.mopl.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codeit.sb06.team03.mopl.enums.ContentStatus;
import org.codeit.sb06.team03.mopl.enums.ContentType;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "contents")
public class Content {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ContentStatus status = ContentStatus.ACTIVE;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private short version;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ContentType type;

    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @Column(name = "description", nullable = false, length = 10_000)
    private String description;

    @NotNull
    @Column(name = "thumbnail_key", nullable = false)
    private String thumbnailKey;

    @OneToMany(mappedBy = "content", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Review> reviews;

    @OneToOne(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    private ReviewStats reviewStats;

    @NotNull
    @Column(name = "average_rating", nullable = false)
    private double averageRating;

    @NotNull
    @Column(name = "review_count", nullable = false)
    private long reviewCount;

    @NotNull
    @Column(name = "watcher_count", nullable = false)
    private long watcherCount; // TODO

    private Content(ContentType type, String title, String description, String thumbnailKey) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.status = ContentStatus.ACTIVE;
        this.type = type;
        this.title = title;
        this.description = description;
        this.thumbnailKey = thumbnailKey;
        this.watcherCount = 0;
        this.reviewCount = 0;
    }

    public static Content create(ContentType contentType, String title, String description, String thumbnailKey) {
        return new Content(
                contentType,
                title,
                description,
                thumbnailKey
        );
    }

    public void update(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public void addReview(int rating) {
        double totalRating = this.averageRating * this.reviewCount;
        this.reviewCount++;
        totalRating += rating;
        this.averageRating = Math.round((totalRating / this.reviewCount) * 10.0) / 10.0;
    }

    public void removeReview(int rating) {
        if (this.reviewCount <= 1) {
            this.reviewCount = 0;
            this.averageRating = 0.0;
        } else {
            double totalRating = this.averageRating * this.reviewCount;
            this.reviewCount--;
            totalRating -= rating;
            this.averageRating = Math.round((totalRating / this.reviewCount) * 10.0) / 10.0;
        }
    }

    public void updateReview(int oldRating, int newRating) {
        if (this.reviewCount > 0) {
            double totalRating = this.averageRating * this.reviewCount;
            totalRating = totalRating - oldRating + newRating;
            this.averageRating = Math.round((totalRating / this.reviewCount) * 10.0) / 10.0;
        }
    }

    public void markAsDeleting() {
        this.status = ContentStatus.DELETING;
    }

    public void markAsDeleted() {
        this.status = ContentStatus.DELETED;
    }

    public void restoreActive() {
        this.status = ContentStatus.ACTIVE;
    }

    public boolean isActive() {
        return this.status == ContentStatus.ACTIVE;
    }

    public boolean isDeleting() {
        return this.status == ContentStatus.DELETING;
    }

    public boolean isDeleted() {
        return this.status == ContentStatus.DELETED;
    }
}
