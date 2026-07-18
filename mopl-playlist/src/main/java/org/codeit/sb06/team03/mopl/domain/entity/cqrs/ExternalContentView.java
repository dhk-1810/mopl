package org.codeit.sb06.team03.mopl.domain.entity.cqrs;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codeit.sb06.team03.mopl.enums.ContentType;

import java.util.UUID;

@Entity
@Table(name = "external_content_views")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExternalContentView {

    @Id
    @Column(name = "content_id")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType type;

    @Column(nullable = false)
    private String title;

    private String description;

    private String thumbnailKey;

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
}
