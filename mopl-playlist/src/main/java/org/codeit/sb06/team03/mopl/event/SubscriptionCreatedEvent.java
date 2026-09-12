package org.codeit.sb06.team03.mopl.event;

import java.util.UUID;

public record SubscriptionCreatedEvent(
        UUID playlistId,
        String playlistTitle,
        UUID subscriberId,
        String subscriberName,
        UUID ownerId
) {}
