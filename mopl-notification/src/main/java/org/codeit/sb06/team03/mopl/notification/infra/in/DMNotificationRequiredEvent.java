package org.codeit.sb06.team03.mopl.notification.infra.in;

import java.util.UUID;

public record DMNotificationRequiredEvent(
        UUID receiverId,
        String senderName,
        String content
) {
}
