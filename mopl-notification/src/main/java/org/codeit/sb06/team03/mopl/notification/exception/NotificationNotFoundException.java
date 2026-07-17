package org.codeit.sb06.team03.mopl.notification.exception;

import java.util.UUID;

public class NotificationNotFoundException extends NotificationException {
    public NotificationNotFoundException(UUID notificationId) {
        super("Notification not found. notificationId: %s".formatted(notificationId));
    }
}
