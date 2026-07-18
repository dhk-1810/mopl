package org.codeit.sb06.team03.mopl.dto.response;

import org.codeit.sb06.team03.mopl.domain.Notification;
import org.codeit.sb06.team03.mopl.domain.NotificationLevel;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto (
        UUID id,
        Instant createdAt,
        UUID receiverId,
        String title,
        String content,
        NotificationLevel level
) {
    public static NotificationDto toDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getCreatedAt(),
                notification.getReceiverId(),
                notification.getTitle(),
                notification.getContent(),
                notification.getLevel()
        );
    }
}
