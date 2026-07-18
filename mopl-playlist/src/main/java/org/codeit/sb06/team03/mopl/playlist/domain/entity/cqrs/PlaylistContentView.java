package org.codeit.sb06.team03.mopl.playlist.domain.entity.cqrs;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codeit.sb06.team03.mopl.content.domain.vo.ContentType;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "playlist_content_views")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaylistContentView {

    @EmbeddedId
    private PlaylistContentId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("playlistId")
    @JoinColumn(name = "playlist_id")
    private PlaylistView playlistView;

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

    private int sortOrder;

    @Embeddable
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class PlaylistContentId implements Serializable {
        private UUID playlistId;
        private UUID contentId;

        public PlaylistContentId(UUID playlistId, UUID contentId) {
            this.playlistId = playlistId;
            this.contentId = contentId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PlaylistContentId that = (PlaylistContentId) o;
            return java.util.Objects.equals(playlistId, that.playlistId) &&
                    java.util.Objects.equals(contentId, that.contentId);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(playlistId, contentId);
        }
    }

    public static PlaylistContentView create(
            PlaylistView playlistView,
            UUID contentId,
            ContentType type,
            String title,
            String description,
            String thumbnailKey,
            String tags,
            double averageRating,
            long reviewCount,
            long watcherCount,
            int sortOrder
    ) {
        PlaylistContentView view = new PlaylistContentView();
        view.id = new PlaylistContentId(playlistView.getId(), contentId);
        view.playlistView = playlistView;
        view.type = type;
        view.title = title;
        view.description = description;
        view.thumbnailKey = thumbnailKey;
        view.tags = tags;
        view.averageRating = averageRating;
        view.reviewCount = reviewCount;
        view.watcherCount = watcherCount;
        view.sortOrder = sortOrder;
        return view;
    }
}
