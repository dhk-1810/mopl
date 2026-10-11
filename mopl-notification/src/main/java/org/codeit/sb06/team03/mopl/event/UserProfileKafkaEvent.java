package org.codeit.sb06.team03.mopl.event;

import java.util.UUID;

public record UserProfileKafkaEvent(
        String eventType, // "CREATED", "UPDATED"
        UUID userId,
        String name,
        String imageKey
) {}
