package org.codeit.sb06.team03.mopl.event;

import java.util.Set;
import java.util.UUID;

public record PlaylistCreatedEvent(
        UUID ownerId,
        String ownerName,
        UUID playlistId,
        String playlistTitle,
        Set<UUID> followerIds
) {
    public PlaylistCreatedEvent(UUID ownerId, String ownerName, UUID playlistId, String playlistTitle) {
        this(ownerId, ownerName, playlistId, playlistTitle, null);
    }
}
