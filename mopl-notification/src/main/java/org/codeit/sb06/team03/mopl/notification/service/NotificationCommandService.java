package org.codeit.sb06.team03.mopl.notification.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.error.InvalidIdentifierException;
import org.codeit.sb06.team03.mopl.notification.domain.Notification;
import org.codeit.sb06.team03.mopl.notification.domain.NotificationLevel;
import org.codeit.sb06.team03.mopl.notification.domain.NotificationService;
import org.codeit.sb06.team03.mopl.notification.exception.NotificationAccessDeniedException;
import org.codeit.sb06.team03.mopl.notification.exception.NotificationNotFoundException;
import org.codeit.sb06.team03.mopl.notification.controller.NotificationDto;
import org.codeit.sb06.team03.mopl.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional("notificationTransactionManager")
public class NotificationCommandService {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public NotificationDto create(UUID receiverId, String title, String content, NotificationLevel level) {
        Notification notification = notificationService.create(receiverId, title, content, level);
        notificationRepository.save(notification);
        return NotificationDto.toDto(notification);
    }

    public List<NotificationDto> createAll(List<UUID> receiverIds, String title, String content, NotificationLevel level) {
        List<Notification> notifications = receiverIds.stream()
                .map(id -> notificationService.create(id, title, content, level))
                .toList();
        notificationRepository.saveAll(notifications);
        return notifications.stream().map(NotificationDto::toDto).toList();
    }

    public void delete(String notificationId, UUID receiverId) {

        UUID notificationUUID = parseUUID(notificationId);
        Notification notification = notificationRepository.findById(notificationUUID)
                .orElseThrow(() -> new NotificationNotFoundException(notificationUUID));

        if (!notification.getReceiverId().equals(receiverId)) {
            throw new NotificationAccessDeniedException(notificationUUID, receiverId);
        }
        notificationRepository.deleteById(notificationUUID);
    }

    private UUID parseUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidIdentifierException(id);
        }
    }
}
