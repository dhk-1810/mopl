package org.codeit.sb06.team03.mopl.liveChatRoom.exception;

import java.util.UUID;

public class LiveChatRoomNotFoundException extends LiveChatRoomException {

    private static final String fromIdFormat = "Live Chat을 찾을 수 없습니다. userId: '%s'";

    public LiveChatRoomNotFoundException(String message) {
        super(message);
    }

    public static LiveChatRoomNotFoundException fromId(UUID id) {
        return new LiveChatRoomNotFoundException(fromIdFormat.formatted(id.toString()));
    }
}
