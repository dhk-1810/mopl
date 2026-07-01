package org.codeit.sb06.team03.mopl.content.domain.exception;

import java.util.UUID;

public class ReviewNotFoundException extends ContentException {

    private static final String fromIdFormat = "Review를 찾을 수 없습니다. id: '%s'";

    public ReviewNotFoundException(String message) {
        super(message);
    }

    public static ReviewNotFoundException fromId(UUID id) {
        return new ReviewNotFoundException(fromIdFormat.formatted(id));
    }
}
