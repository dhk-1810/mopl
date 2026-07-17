package org.codeit.sb06.team03.mopl.notification.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.notification.infra.in.CursorRequestNotificationDto;
import org.codeit.sb06.team03.mopl.notification.infra.in.CursorResponseNotificationDto;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class NotificationCompositeService {

    private final NotificationQueryService notificationQueryService;
    private final NotificationCommandService notificationCommandService;

    public CursorResponseNotificationDto getNotifications(CursorRequestNotificationDto request, UUID receiverId) {
        return notificationQueryService.get(request, receiverId);
    }

    public void deleteNotification(String notificationId, UUID receiverId) {
        notificationCommandService.delete(notificationId, receiverId);
    }
}
