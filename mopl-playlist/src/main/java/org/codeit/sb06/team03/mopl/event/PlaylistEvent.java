package org.codeit.sb06.team03.mopl.event;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract sealed class PlaylistEvent {

    @Getter
    @RequiredArgsConstructor
    public static final class PlaylistCreatedEvent extends PlaylistEvent {
        private final UUID ownerId;
        private final String ownerName;
        private final UUID playlistId;
        private final String playlistTitle;
        private final List<UUID> followerIds;

        public PlaylistCreatedEvent(UUID ownerId, String ownerName, UUID playlistId, String playlistTitle) {
            this.ownerId = ownerId;
            this.ownerName = ownerName;
            this.playlistId = playlistId;
            this.playlistTitle = playlistTitle;
            this.followerIds = null;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static final class SubscriptionCreatedEvent extends PlaylistEvent {
        private final UUID playlistId;
        private final String playlistTitle;
        private final UUID subscriberId;
        private final String subscriberName;
        private final UUID ownerId;
    }

    @Getter
    @RequiredArgsConstructor
    public static final class CurationAddedEvent extends PlaylistEvent {
        private final UUID playlistId;
        private final String playlistTitle;
        private final String contentTitle;
        private final List<UUID> subscriberIds;

        public CurationAddedEvent(UUID playlistId, String playlistTitle, String contentTitle) {
            this.playlistId = playlistId;
            this.playlistTitle = playlistTitle;
            this.contentTitle = contentTitle;
            this.subscriberIds = null;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static final class PlaylistDeletedEvent extends PlaylistEvent {
        private final UUID playlistId;
    }
}
