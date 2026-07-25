package org.codeit.sb06.team03.mopl.event;

import java.util.UUID;

public class UserEvent {

    public record UserProfileCreatedEvent(
            UUID userId,
            String name,
            String imageKey
    ) {}

    public record UserProfileUpdatedEvent(
            UUID userId,
            String name,
            String imageKey
    ) {}
}
