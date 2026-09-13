package org.codeit.sb06.team03.mopl.sse.dto;

import java.util.UUID;

/**
 * 타겟 서버 인스턴스로 RabbitMQ 메시지 전달하기 위한 DTO.
 */
public record NotificationSsePayload(
        UUID receiverId,
        String eventName,
        Object data,
        String eventId
) {
}
