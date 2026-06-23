package org.codeit.sb06.team03.mopl.content;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codeit.sb06.team03.mopl.content.domain.entity.Review;
import org.codeit.sb06.team03.mopl.content.domain.entity.ReviewStats;
import org.codeit.sb06.team03.mopl.content.domain.entity.Tag;
import org.codeit.sb06.team03.mopl.content.domain.vo.ContentType;

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
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

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

    @ManyToMany
    @JoinTable(
            name = "contents_tags",
            joinColumns = @JoinColumn(name = "content_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags;

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
    private long watcherCount;

    private Content(ContentType type, String title, String description, String thumbnailKey) {
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
}
