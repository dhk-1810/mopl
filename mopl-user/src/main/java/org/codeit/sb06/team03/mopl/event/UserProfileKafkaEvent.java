package org.codeit.sb06.team03.mopl.event;

import java.util.UUID;

public record UserProfileKafkaEvent(
        String eventType, // "CREATED", "UPDATED"
        UUID userId,
        String name,
        String imageKey
) {
    public static UserProfileKafkaEvent created(UUID userId, String name, String imageKey) {
        return new UserProfileKafkaEvent("CREATED", userId, name, imageKey);
    }

    public static UserProfileKafkaEvent updated(UUID userId, String name, String imageKey) {
        return new UserProfileKafkaEvent("UPDATED", userId, name, imageKey);
    }
}
