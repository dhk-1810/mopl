package org.codeit.sb06.team03.mopl.event;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract sealed class UserEvent {

    @Getter
    @RequiredArgsConstructor
    public static final class ProfileCreatedEvent extends UserEvent {
        private final UUID userId;
        private final String name;
        private final String imageKey;
    }

    @Getter
    @RequiredArgsConstructor
    public static final class ProfileUpdatedEvent extends UserEvent {
        private final UUID userId;
        private final String name;
        private final String imageKey;
    }
}
