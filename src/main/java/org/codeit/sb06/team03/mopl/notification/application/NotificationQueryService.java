package org.codeit.sb06.team03.mopl.notification.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.notification.application.in.GetNotificationsUseCase;
import org.codeit.sb06.team03.mopl.notification.infra.in.CursorRequestNotificationDto;
import org.codeit.sb06.team03.mopl.notification.infra.in.CursorResponseNotificationDto;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class NotificationQueryService implements GetNotificationsUseCase {

    @Override
    public CursorResponseNotificationDto get(CursorRequestNotificationDto request, UUID ownerId) {
        return null;
    }
}
