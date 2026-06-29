package org.codeit.sb06.team03.mopl.content.domain.exception;

import java.util.UUID;

public class ReviewAlreadyExistsException extends ContentException {

    private static final String format = "이미 해당 콘텐츠에 작성한 리뷰가 존재합니다. contentId: '%s', authorId: '%s'";

    public ReviewAlreadyExistsException(String message) {
        super(message);
    }

    public static ReviewAlreadyExistsException fromContentIdAndAuthorId(UUID contentId, UUID authorId) {
        return new ReviewAlreadyExistsException(format.formatted(contentId, authorId));
    }
}
