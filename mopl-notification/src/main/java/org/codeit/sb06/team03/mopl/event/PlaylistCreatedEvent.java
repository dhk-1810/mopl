package org.codeit.sb06.team03.mopl.event;

import java.util.List;
import java.util.UUID;

public record PlaylistCreatedEvent(
        UUID ownerId,
        String ownerName,
        UUID playlistId,
        String playlistTitle,
        List<UUID> followerIds
) {}
