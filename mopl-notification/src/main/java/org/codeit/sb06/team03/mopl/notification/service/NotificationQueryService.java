package org.codeit.sb06.team03.mopl.notification.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.error.InvalidIdentifierException;
import org.codeit.sb06.team03.mopl.notification.controller.CursorRequestNotificationDto;
import org.codeit.sb06.team03.mopl.notification.controller.CursorResponseNotificationDto;
import org.codeit.sb06.team03.mopl.notification.controller.NotificationDto;
import org.codeit.sb06.team03.mopl.notification.service.CursorGetNotificationsCondition;
import org.codeit.sb06.team03.mopl.notification.repository.NotificationRepository;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;

    @Transactional(value = "notificationTransactionManager", readOnly = true)
    public CursorResponseNotificationDto get(CursorRequestNotificationDto request, UUID receiverId) {

        final UUID idAfter = request.idAfter() != null ? parseUUID(request.idAfter()): null;
        CursorGetNotificationsCondition condition = new CursorGetNotificationsCondition(
                receiverId,
                request.cursor(),
                idAfter,
                request.limit(),
                request.sortBy(),
                request.sortDirection().equals("DESCENDING")
        );

        Slice<NotificationDto> content = notificationRepository.findAll(condition);

        String nextCursor = null;
        UUID nextIdAfter = null;
        boolean hasNext = content.hasNext();
        List<NotificationDto> data = content.getContent();
        if (hasNext) {
            nextCursor = data.get(content.getSize() - 1).createdAt().toString();
            nextIdAfter = data.get(content.getSize() - 1).id();
        }

        long totalCount = notificationRepository.countByReceiverId(receiverId);
        return new CursorResponseNotificationDto(
                data,
                nextCursor,
                nextIdAfter,
                hasNext,
                totalCount,
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
