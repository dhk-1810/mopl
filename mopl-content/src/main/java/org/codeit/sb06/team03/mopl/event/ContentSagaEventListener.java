package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.entity.Content;
import org.codeit.sb06.team03.mopl.repository.ContentRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentSagaEventListener {

    private final ContentRepository contentRepository;

    @RabbitListener(queues = RabbitConfig.CONTENT_SAGA_RESPONSE_QUEUE)
    @Transactional("contentTransactionManager")
    public void handleSagaResponse(ContentDeletionSagaEvent event) {
        log.info("Received ContentDeletionSagaEvent response from [{}]: {}", event.participant(), event);

        Content content = contentRepository.findById(event.contentId()).orElse(null);
        if (content == null) {
            log.warn("Content not found for saga response, contentId: {}", event.contentId());
            return;
        }

        if ("FAILED".equals(event.status())) {
            log.warn("Saga step failed in participant [{}]. Executing Compensating Transaction for contentId: {}", 
                    event.participant(), event.contentId());
            // Compensating Transaction: 롤백 (소프트 딜리트 취소 및 복구)
            content.restoreActive();
            contentRepository.save(content);
            log.info("Successfully restored Content to ACTIVE status. ContentId: {}", event.contentId());
        } else if ("SUCCESS".equals(event.status())) {
            log.info("Saga step succeeded in participant [{}]. Marking Content as DELETED for contentId: {}", event.participant(), event.contentId());
            content.markAsDeleted();
            contentRepository.save(content);
        }
    }
}
