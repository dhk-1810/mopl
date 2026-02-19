package org.codeit.sb06.team03.mopl.notification.domain.exception;

import java.util.UUID;

public class NotificationAccessDeniedException extends NotificationException {
    public NotificationAccessDeniedException(UUID notificationId, UUID userId) {
        super("알림은 소유자만 삭제할 수 있습니다. notificationId: %s, userId: %s".formatted(notificationId, userId));
    }
}
