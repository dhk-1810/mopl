package org.codeit.sb06.team03.mopl.notification.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.notification.application.in.GetNotificationsUseCase;
import org.codeit.sb06.team03.mopl.notification.application.out.LoadNotificationsPort;
import org.codeit.sb06.team03.mopl.notification.domain.Notification;
import org.codeit.sb06.team03.mopl.notification.infra.in.CursorRequestNotificationDto;
import org.codeit.sb06.team03.mopl.notification.infra.in.CursorResponseNotificationDto;
import org.codeit.sb06.team03.mopl.notification.infra.in.NotificationDto;
import org.codeit.sb06.team03.mopl.notification.infra.out.CursorGetNotificationsCondition;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class NotificationQueryService implements GetNotificationsUseCase {

    private final LoadNotificationsPort loadNotificationsPort;

    @Override
    public CursorResponseNotificationDto get(CursorRequestNotificationDto request, UUID ownerId) {

        CursorGetNotificationsCondition condition = new CursorGetNotificationsCondition(
                ownerId,
                request.cursor(),
                request.idAfter(),
                request.limit(),
                request.sortBy(),
                request.sortDirection()
        );

        Slice<Notification> content = loadNotificationsPort.getNotifications(condition);

        String nextCursor = null;
        UUID nextIdAfter = null;
        boolean hasNext = content.hasNext();
        List<Notification> notifications = content.getContent();
        if (hasNext) {
            nextCursor = notifications.get(content.getSize() - 1).getCreatedAt().toString();
            nextIdAfter = notifications.get(content.getSize() - 1).getId();
        }

        List<NotificationDto> data = notifications.stream().map(NotificationDto::toDto).toList();
        return new CursorResponseNotificationDto(
                data,
                nextCursor,
                nextIdAfter,
                hasNext,
                0, // TODO
                request.sortBy(),
                request.sortDirection()
        );
    }
}
