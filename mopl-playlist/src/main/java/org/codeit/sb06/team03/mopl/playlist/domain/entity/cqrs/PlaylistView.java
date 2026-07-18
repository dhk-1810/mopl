package org.codeit.sb06.team03.mopl.playlist.domain.entity.cqrs;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "playlist_views")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaylistView {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = false)
    private long subscriberCount;

    // Denormalized Owner Info (User service)
    @Column(nullable = false)
    private UUID ownerId;

    @Column(nullable = false)
    private String ownerName;

    private String ownerProfileImageKey;

    @OneToMany(mappedBy = "playlistView", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<PlaylistContentView> contents = new ArrayList<>();

    public static PlaylistView create(UUID id, String title, String description, UUID ownerId, String ownerName, String ownerProfileImageKey) {
        PlaylistView view = new PlaylistView();
        view.id = id;
        view.title = title;
        view.description = description;
        view.updatedAt = Instant.now();
        view.subscriberCount = 0;
        view.ownerId = ownerId;
        view.ownerName = ownerName;
        view.ownerProfileImageKey = ownerProfileImageKey;
        return view;
    }

    public void updateMetadata(String title, String description, Instant updatedAt) {
        this.title = title;
        this.description = description;
        this.updatedAt = updatedAt;
    }

    public void updateOwnerInfo(String name, String profileImageKey) {
        this.ownerName = name;
        this.ownerProfileImageKey = profileImageKey;
    }

    public void updateSubscriberCount(long subscriberCount) {
        this.subscriberCount = subscriberCount;
    }
}
