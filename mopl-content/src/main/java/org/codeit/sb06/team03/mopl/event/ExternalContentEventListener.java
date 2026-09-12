package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.dto.response.ContentDto;
import org.codeit.sb06.team03.mopl.entity.ContentReadModel;
import org.codeit.sb06.team03.mopl.service.application.ContentQueryService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalContentEventListener {

    private final ContentQueryService contentQueryService;

    @RabbitListener(queues = RabbitConfig.CONTENT_RPC_QUEUE)
    @Transactional(value = "contentTransactionManager", readOnly = true)
    public ContentDto handleGetContentById(UUID contentId) {
        log.info("Received RPC request for contentId: {}", contentId);
        try {
            ContentReadModel content = contentQueryService.get(contentId);
            return ContentDto.from(content, content.thumbnailKey());
        } catch (Exception e) {
            log.warn("Failed to find content via RPC for contentId: {}, cause: {}", contentId, e.getMessage());
            return null;
        }
    }
}
