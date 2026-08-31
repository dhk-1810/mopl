package org.codeit.sb06.team03.mopl.service.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.entity.OutboxEvent;
import org.codeit.sb06.team03.mopl.enums.OutboxStatus;
import org.codeit.sb06.team03.mopl.repository.OutboxEventRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional(value = "contentTransactionManager")
    public OutboxEvent saveEvent(String aggregateType, String aggregateId, String eventType, String exchange, String routingKey, Object payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            OutboxEvent outboxEvent = OutboxEvent.create(aggregateType, aggregateId, eventType, exchange, routingKey, jsonPayload);
            OutboxEvent saved = outboxEventRepository.save(outboxEvent);
            log.info("Saved outbox event [id: {}, type: {}, aggregateId: {}]", saved.getId(), eventType, aggregateId);
            return saved;
        } catch (Exception e) {
            log.error("Failed to serialize and save outbox event for aggregate {}:{}", aggregateType, aggregateId, e);
            throw new RuntimeException("Failed to serialize outbox event payload", e);
        }
    }

    @Transactional(value = "contentTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void markAsPublished(UUID outboxId) {
        outboxEventRepository.findById(outboxId).ifPresent(event -> {
            event.markPublished();
            outboxEventRepository.save(event);
            log.info("Marked outbox event as PUBLISHED: {}", outboxId);
        });
    }

    @Transactional(value = "contentTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(UUID outboxId, String errorMessage, int maxRetries) {
        outboxEventRepository.findById(outboxId).ifPresent(event -> {
            event.recordFailure(errorMessage, maxRetries);
            outboxEventRepository.save(event);
            log.warn("Recorded failure for outbox event [id: {}, retryCount: {}, status: {}]", outboxId, event.getRetryCount(), event.getStatus());
        });
    }

    @Transactional(value = "contentTransactionManager", readOnly = true)
    public List<OutboxEvent> findPendingEvents(int maxRetries, int limit) {
        return outboxEventRepository.findByStatusAndRetryCountLessThanOrderByCreatedAtAsc(
                OutboxStatus.PENDING,
                maxRetries,
                PageRequest.of(0, limit)
        );
    }

    @Transactional(value = "contentTransactionManager")
    public int purgeOldPublishedEvents(Instant threshold) {
        return outboxEventRepository.deleteByStatusAndCreatedAtBefore(OutboxStatus.PUBLISHED, threshold);
    }
}
