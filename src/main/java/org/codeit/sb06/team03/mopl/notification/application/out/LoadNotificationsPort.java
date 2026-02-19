package org.codeit.sb06.team03.mopl.notification.application.out;

import org.codeit.sb06.team03.mopl.notification.domain.Notification;
import org.codeit.sb06.team03.mopl.notification.infra.out.CursorGetNotificationsCondition;
import org.springframework.data.domain.Slice;

public interface LoadNotificationsPort {
    Slice<Notification> getNotifications(CursorGetNotificationsCondition condition);
}
