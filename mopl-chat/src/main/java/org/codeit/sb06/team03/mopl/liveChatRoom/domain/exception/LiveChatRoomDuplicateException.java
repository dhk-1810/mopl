package org.codeit.sb06.team03.mopl.liveChatRoom.domain.exception;

import java.util.UUID;

public class LiveChatRoomDuplicateException extends LiveChatRoomException {

    private static final String fromIdFormat = "이미 존재하는 LiveChatRoom 입니다. userId: '%s'";

    public LiveChatRoomDuplicateException(String message) {
        super(message);
    }

    public static LiveChatRoomDuplicateException fromId(UUID liveChatRoomId) {
        return new LiveChatRoomDuplicateException(fromIdFormat.formatted(liveChatRoomId));
    }
}
