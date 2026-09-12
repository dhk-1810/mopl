package org.codeit.sb06.team03.mopl.event;

import java.util.List;
import java.util.UUID;

public record CurationAddedEvent(
        UUID playlistId,
        String playlistTitle,
        String contentTitle,
        List<UUID> subscriberIds
) {}
