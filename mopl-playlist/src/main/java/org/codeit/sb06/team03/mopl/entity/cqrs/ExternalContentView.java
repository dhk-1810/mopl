package org.codeit.sb06.team03.mopl.entity.cqrs;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codeit.sb06.team03.mopl.enums.ContentType;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "external_content_views")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE external_content_views SET deleted_at = NOW() WHERE content_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ExternalContentView {

    @Id
    @Column(name = "content_id")
    private UUID id;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 1024)
    private String thumbnailKey;

    @Column(columnDefinition = "TEXT")
    private String tags; // Comma-separated tag string

    private double averageRating;

    private long reviewCount;

    private long watcherCount;

    public static ExternalContentView create(
            UUID id,
            ContentType type,
            String title,
            String description,
            String thumbnailKey,
            String tags,
            double averageRating,
            long reviewCount,
            long watcherCount
    ) {
        ExternalContentView view = new ExternalContentView();
        view.id = id;
        view.type = type;
        view.title = title;
        view.description = description;
        view.thumbnailKey = thumbnailKey;
        view.tags = tags;
        view.averageRating = averageRating;
        view.reviewCount = reviewCount;
        view.watcherCount = watcherCount;
        return view;
    }

    public void update(
            String title,
            String description,
            String thumbnailKey,
            String tags,
            double averageRating,
            long reviewCount,
            long watcherCount
    ) {
        this.title = title;
        this.description = description;
        this.thumbnailKey = thumbnailKey;
        this.tags = tags;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
        this.watcherCount = watcherCount;
    }

    public void delete() {
        this.deletedAt = Instant.now();
    }

    public void restore() {
        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
