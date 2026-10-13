package org.codeit.sb06.team03.mopl.event;

import java.util.UUID;

public record ProfileKafkaEvent(
        String eventType, // "CREATED", "UPDATED"
        UUID userId,
        String name,
        String imageKey
) {
    public static ProfileKafkaEvent created(UUID userId, String name, String imageKey) {
        return new ProfileKafkaEvent("CREATED", userId, name, imageKey);
    }

    public static ProfileKafkaEvent updated(UUID userId, String name, String imageKey) {
        return new ProfileKafkaEvent("UPDATED", userId, name, imageKey);
    }
}
