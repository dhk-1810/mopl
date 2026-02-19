package org.codeit.sb06.team03.mopl.notification.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.domain.exception.InvalidIdentifierException;
import org.codeit.sb06.team03.mopl.notification.application.in.GetNotificationsUseCase;
import org.codeit.sb06.team03.mopl.notification.application.out.LoadNotificationsPort;
import org.codeit.sb06.team03.mopl.notification.domain.Notification;
import org.codeit.sb06.team03.mopl.notification.infra.in.CursorRequestNotificationDto;
import org.codeit.sb06.team03.mopl.notification.infra.in.CursorResponseNotificationDto;
import org.codeit.sb06.team03.mopl.notification.infra.in.NotificationDto;
import org.codeit.sb06.team03.mopl.notification.infra.out.CursorGetNotificationsCondition;
import org.hibernate.query.SortDirection;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class NotificationQueryService implements GetNotificationsUseCase {

    private final LoadNotificationsPort loadNotificationsPort;

    @Override
    public CursorResponseNotificationDto get(CursorRequestNotificationDto request, UUID receiverId) {

        CursorGetNotificationsCondition condition = new CursorGetNotificationsCondition(
                receiverId,
                request.cursor(),
                parseUUID(request.idAfter()),
                request.limit(),
                request.sortBy(),
                request.sortDirection().equals("DESCENDING")
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

    private UUID parseUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidIdentifierException(id);
        }
    }
}
