package org.codeit.sb06.team03.mopl.notification.controller;

import java.util.UUID;

public record DMNotificationRequiredEvent(
        UUID receiverId,
        String senderName,
        String content
) {
}
