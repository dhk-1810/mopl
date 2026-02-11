package org.codeit.sb06.team03.mopl.playlist.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "playlists")
public class Playlist {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "subscriber_count", nullable = false)
    private long subscriberCount;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "content_id")
//    private List<Content> contents = new ArrayList<>();

    private Playlist(String title, String description, UUID ownerId) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.title = title;
        this.description = description;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.subscriberCount = 0;
    }

    public static Playlist create(String title, String description, UUID ownerId) {
        return new Playlist(title, description, ownerId);
    }

    public void update(String title, String description) {
        if (title != null && !title.isEmpty()) {
            this.title = title;
        }
        if (description != null && !description.isEmpty()) {
            this.description = description;
        }
    }

    public void increaseSubscriberCount() {
        this.subscriberCount++;
    }

    public void decreaseSubscriberCount() {
        if (subscriberCount > 0) {
            this.subscriberCount--;
        }
    }

}
