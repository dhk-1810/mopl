package org.codeit.sb06.team03.mopl.notification.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.notification.controller.CursorRequestNotificationDto;
import org.codeit.sb06.team03.mopl.notification.controller.CursorResponseNotificationDto;
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
