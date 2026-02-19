package org.codeit.sb06.team03.mopl.notification.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.domain.exception.InvalidIdentifierException;
import org.codeit.sb06.team03.mopl.notification.application.in.DeleteNotificationUseCase;
import org.codeit.sb06.team03.mopl.notification.application.out.DeleteNotificationPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class NotificationCommandService implements DeleteNotificationUseCase {

    private final DeleteNotificationPort deleteNotificationPort;

    @Override
    public void delete(String notificationId, UUID ownerId) {
        UUID notificationUUID = parseUUID(notificationId);
        deleteNotificationPort.delete(notificationUUID, ownerId);
    }

    private UUID parseUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidIdentifierException(id);
        }
    }
}
