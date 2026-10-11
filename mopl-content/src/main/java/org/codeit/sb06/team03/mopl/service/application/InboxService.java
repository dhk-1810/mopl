package org.codeit.sb06.team03.mopl.service.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.entity.InboxEvent;
import org.codeit.sb06.team03.mopl.repository.InboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class InboxService {

    private final InboxEventRepository inboxEventRepository;

    @Transactional(value = "contentTransactionManager", readOnly = true)
    public boolean isAlreadyProcessed(String messageId) {
        return inboxEventRepository.existsByMessageId(messageId);
    }

    @Transactional(value = "contentTransactionManager")
    public void recordProcessed(String messageId, String aggregateType, String eventType, String payload) {
        InboxEvent inboxEvent = InboxEvent.create(messageId, aggregateType, eventType, payload);
        inboxEventRepository.save(inboxEvent);
        log.info("Recorded inbox event [messageId: {}, aggregateType: {}]", messageId, aggregateType);
    }

    @Transactional(value = "contentTransactionManager")
    public int purgeOldInboxEvents(Instant threshold) {
        return inboxEventRepository.deleteByProcessedAtBefore(threshold);
    }
}
