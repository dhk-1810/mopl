package org.codeit.sb06.team03.mopl.service.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.entity.Notification;
import org.codeit.sb06.team03.mopl.enums.NotificationLevel;
import org.codeit.sb06.team03.mopl.exception.NotificationAccessDeniedException;
import org.codeit.sb06.team03.mopl.exception.NotificationNotFoundException;
import org.codeit.sb06.team03.mopl.dto.response.NotificationDto;
import org.codeit.sb06.team03.mopl.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional("notificationTransactionManager")
public class NotificationCommandService {

    private final NotificationRepository notificationRepository;

    public NotificationDto create(UUID receiverId, String title, String content, NotificationLevel level) {
        Notification notification = Notification.create(receiverId, title, content, level);
        notificationRepository.save(notification);
        return NotificationDto.toDto(notification);
    }

    public List<NotificationDto> createAll(List<UUID> receiverIds, String title, String content, NotificationLevel level) {
        List<Notification> notifications = receiverIds.stream()
                .map(id -> Notification.create(id, title, content, level))
                .toList();
        notificationRepository.saveAll(notifications);
        return notifications.stream().map(NotificationDto::toDto).toList();
    }

    public void delete(UUID notificationId, UUID receiverId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        if (!notification.getReceiverId().equals(receiverId)) {
            throw new NotificationAccessDeniedException(notificationId, receiverId);
        }
        notificationRepository.deleteById(notificationId);
    }

}
