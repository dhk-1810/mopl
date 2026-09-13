package org.codeit.sb06.team03.mopl.sse;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 알림 서버 인스턴스에 할당되는 고유 ID.
 * SSE 알림을 핀포인트 발송하기 위해 사용.
 */
@Getter
@Component
public class NotificationInstanceId {

    private final String id;

    public NotificationInstanceId() {
        this.id = UUID.randomUUID().toString();
    }
}
