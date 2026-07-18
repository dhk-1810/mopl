package org.codeit.sb06.team03.mopl.event;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.UserSummary;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract sealed class DMMessageEvent {

    @Getter
    @RequiredArgsConstructor
    public static final class MessageSentEvent extends DMMessageEvent {
        private final UUID messageId;
        private final UUID dmChatRoomId;
        private final UUID senderId;
        private final UUID receiverId;
        private final String content;
        private final Instant createdAt;
        private final UserSummary sender;
        private final UserSummary receiver;
    }

    @Getter
    @RequiredArgsConstructor
    public static final class MessageReceivedEvent extends DMMessageEvent {
        private final UUID messageId;
        private final UUID dmChatRoomId;
        private final UUID senderId;
        private final UUID receiverId;
    }

    @Getter
    @RequiredArgsConstructor
    public static final class MessagePassedEvent extends DMMessageEvent {
        private final UUID messageId;
        private final UUID dmChatRoomId;
        private final UUID receiverId;
        private final String content;
    }
}


