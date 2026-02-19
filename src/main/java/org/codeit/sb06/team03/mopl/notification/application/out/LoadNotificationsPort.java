package org.codeit.sb06.team03.mopl.notification.application.out;

import org.codeit.sb06.team03.mopl.notification.domain.Notification;
import org.springframework.data.domain.Slice;

import java.util.UUID;

public interface LoadNotificationsPort {
    Slice<Notification> getNotifications(UUID ownerId);
}
