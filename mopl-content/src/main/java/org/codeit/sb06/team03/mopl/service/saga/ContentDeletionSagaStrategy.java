package org.codeit.sb06.team03.mopl.service.saga;

import java.util.UUID;

// Saga 패턴의 여러 구현체 (gRPC, Kafka) 유지를 위한 인터페이스
public interface ContentDeletionSagaStrategy {
    void delete(UUID contentId);
}
