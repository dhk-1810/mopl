package org.codeit.sb06.team03.mopl.playlist.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codeit.sb06.team03.mopl.account.domain.Account;

import java.time.Instant;
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
    private UUID ownerId; // TODO 확인 필요

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
//    private Set<Content> contents;

    private Playlist(String title, String description) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.description = description;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.subscriberCount = 0;
    }

    public static Playlist create(String title, String description) {
        return new Playlist(title, description);
    }

}
