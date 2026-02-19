package org.codeit.sb06.team03.mopl.notification.application.in;

import org.codeit.sb06.team03.mopl.notification.domain.NotificationLevel;

import java.util.UUID;

public interface CreateNotificationUseCase {
    void create(UUID receiverId, String title, String content, NotificationLevel level);
}
