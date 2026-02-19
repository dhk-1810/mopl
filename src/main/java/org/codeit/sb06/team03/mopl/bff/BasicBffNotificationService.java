package org.codeit.sb06.team03.mopl.bff;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.notification.application.in.DeleteNotificationUseCase;
import org.codeit.sb06.team03.mopl.notification.application.in.GetNotificationsUseCase;
import org.codeit.sb06.team03.mopl.notification.infra.in.CursorRequestNotificationDto;
import org.codeit.sb06.team03.mopl.notification.infra.in.CursorResponseNotificationDto;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BasicBffNotificationService implements BffNotificationService {

    private final GetNotificationsUseCase getNotificationsUseCase;
    private final DeleteNotificationUseCase deleteNotificationUseCase;

    @Override
    public CursorResponseNotificationDto getNotifications(CursorRequestNotificationDto request, UUID ownerId) {
        return getNotificationsUseCase.get(request, ownerId);
    }

    @Override
    public void deleteNotification(String notificationId, UUID ownerId) {
        deleteNotificationUseCase.delete(notificationId, ownerId);
    }
}
