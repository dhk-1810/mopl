package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.dto.request.ContentCreateInternalRequest;
import org.codeit.sb06.team03.mopl.service.composite.ContentCompositeService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentBatchEventListener {

    private final ContentCompositeService contentCompositeService;

    @RabbitListener(queues = RabbitConfig.CONTENT_BATCH_INFO_QUEUE)
    public void handleContentBatchInfo(ContentCreateInternalRequest request) {
        log.info("Received content batch message from RabbitMQ: title={}", request.title());
        try {
            contentCompositeService.createInternal(request);
            log.info("Successfully created content from batch message: title={}", request.title());
        } catch (Exception e) {
            log.error("Failed to process content batch message: title={}", request.title(), e);
        }
    }
}
