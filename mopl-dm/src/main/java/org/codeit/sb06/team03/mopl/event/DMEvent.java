package org.codeit.sb06.team03.mopl.event;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.UserSummary;
import org.codeit.sb06.team03.mopl.dto.response.DirectMessageDto;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract sealed class DMEvent {

    @Getter
    @RequiredArgsConstructor
    public static final class ChatRoomCreatedEvent extends DMEvent {
        private final UUID dmChatRoomId;
        private final UUID userId;
        private final UUID withUserId;
    }

    @Getter
    @RequiredArgsConstructor
    public static final class ChatRoomJoinedEvent extends DMEvent {
        private final UUID dmChatRoomId;
        private final UUID userId;
    }

    @Getter
    @RequiredArgsConstructor
    public static final class ChatRoomLeftEvent extends DMEvent {
        private final UUID dmChatRoomId;
        private final UUID userId;
    }

    @Getter
    @RequiredArgsConstructor
    public static final class MessageReadEvent extends DMEvent {
        private final UUID dmChatRoomId;
        private final UUID userId;
    }

    @Getter
    @RequiredArgsConstructor
    public static final class NewMessageMarkEvent extends DMEvent {
        private final UUID receiverId;
        private final String senderName;
        private final String content;
        private final DirectMessageDto directMessage;
    }

    @Getter
    @RequiredArgsConstructor
    public static final class MessageSentEvent extends DMEvent {
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
    public static final class MessageReceivedEvent extends DMEvent {
        private final UUID messageId;
        private final UUID dmChatRoomId;
        private final UUID senderId;
        private final UUID receiverId;
    }

    @Getter
    @RequiredArgsConstructor
    public static final class MessagePassedEvent extends DMEvent {
        private final UUID messageId;
        private final UUID dmChatRoomId;
        private final UUID receiverId;
        private final String content;
    }
}
