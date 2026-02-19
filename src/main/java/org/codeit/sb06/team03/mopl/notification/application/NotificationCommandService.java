package org.codeit.sb06.team03.mopl.notification.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.domain.exception.InvalidIdentifierException;
import org.codeit.sb06.team03.mopl.notification.application.in.DeleteNotificationUseCase;
import org.codeit.sb06.team03.mopl.notification.application.out.DeleteNotificationPort;
import org.codeit.sb06.team03.mopl.notification.application.out.LoadSingleNotificationPort;
import org.codeit.sb06.team03.mopl.notification.domain.Notification;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class NotificationCommandService implements DeleteNotificationUseCase {

    private final LoadSingleNotificationPort loadSingleNotificationPort;
    private final DeleteNotificationPort deleteNotificationPort;


    @Override
    public void delete(String notificationId, UUID ownerId) {

        UUID notificationUUID = parseUUID(notificationId);
        Notification notification = loadSingleNotificationPort.load(notificationUUID)
                .orElseThrow(() -> new NotificationNotFoundException(notificationUUID));

        if (!notification.getReceiverId().equals(ownerId)) {
            throw new NotificationAccessDeniedException(notificationUUID);
        }
        deleteNotificationPort.deleteById(notificationUUID);
    }

    private UUID parseUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidIdentifierException(id);
        }
    }
}
