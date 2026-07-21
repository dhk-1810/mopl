package org.codeit.sb06.team03.mopl.exception;

public class WatchingSessionAccessDeniedException extends WatchingSessionException {

    public WatchingSessionAccessDeniedException(String message) {
        super(message);
    }

    public WatchingSessionAccessDeniedException() {
        super("해당 워칭 세션에 대한 접근 권한이 없습니다.");
    }
}
