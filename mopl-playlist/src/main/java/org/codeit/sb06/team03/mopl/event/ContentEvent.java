package org.codeit.sb06.team03.mopl.event;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract sealed class ContentEvent {

    @Getter
    @RequiredArgsConstructor
    public static final class ContentUpdatedEvent extends ContentEvent {
        private final UUID contentId;
        private final String type;
        private final String title;
        private final String description;
        private final String thumbnailKey;
        private final Set<String> tags;
        private final double averageRating;
        private final long reviewCount;
        private final long watcherCount;
    }

    @Getter
    @RequiredArgsConstructor
    public static final class ContentDeletedEvent extends ContentEvent {
        private final UUID contentId;
    }
}
