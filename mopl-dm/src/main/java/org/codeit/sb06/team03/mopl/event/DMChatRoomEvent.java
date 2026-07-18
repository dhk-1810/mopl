package org.codeit.sb06.team03.mopl.event;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract sealed class DMChatRoomEvent {

    @Getter
    @RequiredArgsConstructor
    public static final class DMChatRoomCreatedEvent extends DMChatRoomEvent {
        private final UUID dmChatRoomId;
        private final UUID userId;
        private final UUID withUserId;
    }

    @Getter
    @RequiredArgsConstructor
    public static final class DMMessageJoinedEvent extends DMChatRoomEvent {
        private final UUID dmChatRoomId;
        private final UUID userId;
    }

    @Getter
    @RequiredArgsConstructor
    public static final class DMMessageLeftEvent extends DMChatRoomEvent {
        private final UUID dmChatRoomId;
        private final UUID userId;
    }

    @Getter
    @RequiredArgsConstructor
    public static final class MessageReadedEvent extends DMChatRoomEvent {
        private final UUID dmChatRoomId;
        private final UUID userId;
    }
}
