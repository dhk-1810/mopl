package org.codeit.sb06.team03.mopl.exception;

import java.util.UUID;

public class WatchingSessionDuplicateException extends WatchingSessionException {

    private static final String fromLiveChatRoomIdAndAccountIdFormat
            = "중복된 WatchingSession입니다. liveChatRoomId: '%s', accountId: '%s'";

    public WatchingSessionDuplicateException(String message) {
        super(message);
    }

    public static WatchingSessionDuplicateException fromLiveChatRoomIdAndAccountId(UUID liveChatRoomId, UUID accountId) {
        return new WatchingSessionDuplicateException(
                fromLiveChatRoomIdAndAccountIdFormat.formatted(liveChatRoomId, accountId)
        );
    }
}
