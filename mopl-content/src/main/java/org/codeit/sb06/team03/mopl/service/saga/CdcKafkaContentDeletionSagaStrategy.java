package org.codeit.sb06.team03.mopl.service.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.service.application.ContentCommandService;
import org.codeit.sb06.team03.mopl.service.application.ContentQueryService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mopl.saga.type", havingValue = "cdc-kafka")
public class CdcKafkaContentDeletionSagaStrategy implements ContentDeletionSagaStrategy {

    private final ContentQueryService contentQueryService;
    private final ContentCommandService contentCommandService;

    @Override
    public void deleteContent(UUID contentId) {
        // 1. 대상 컨텐츠 존재 여부 사전 확인
        contentQueryService.get(contentId);

        // 2. CDC(Debezium) 기반 비동기 Saga 트리거 (DELETING 마킹 & Outbox Event 저장 -> WAL을 거쳐 Kafka로 스트리밍)
        UUID sagaId = contentCommandService.deleteSaga(contentId);
        log.info("[Saga Strategy: CDC-Kafka] Triggered CDC saga deletion for content: {}, sagaId: {}", contentId, sagaId);
    }
}
