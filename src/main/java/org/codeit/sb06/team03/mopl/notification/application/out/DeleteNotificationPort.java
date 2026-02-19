package org.codeit.sb06.team03.mopl.notification.application.out;

import java.util.UUID;

public interface DeleteNotificationPort {
    void delete(UUID notificationId, UUID ownerId);
}
