package org.codeit.sb06.team03.mopl.event;

import java.util.UUID;

public interface UserEvent {
    UUID userId();

    record UserProfileCreatedEvent(
            UUID userId,
            String name,
            String imageKey
    ) implements UserEvent {}

    record UserProfileUpdatedEvent(
            UUID userId,
            String name,
            String imageKey
    ) implements UserEvent {}
}
