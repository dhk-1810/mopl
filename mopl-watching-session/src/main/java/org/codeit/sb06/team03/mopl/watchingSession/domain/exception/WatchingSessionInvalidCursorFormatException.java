package org.codeit.sb06.team03.mopl.watchingSession.domain.exception;

public class WatchingSessionInvalidCursorFormatException extends WatchingSessionException {

    private static final String messageFormat = "지원하지 않는 Cursor Format입니다. data: '%s'";

    public WatchingSessionInvalidCursorFormatException(String message) {
        super(message);
    }

    public static WatchingSessionInvalidCursorFormatException from (String data) {
        return new WatchingSessionInvalidCursorFormatException(messageFormat.formatted(data));
    }
}
