package org.codeit.sb06.team03.mopl.playlist.infra.in;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.content.domain.event.ContentDeletedEvent;
import org.codeit.sb06.team03.mopl.playlist.RabbitConfig;
import org.codeit.sb06.team03.mopl.playlist.application.in.DeleteCurationUseCase;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentDeletedEventListener {

    private final DeleteCurationUseCase deleteCurationUseCase;

    @RabbitListener(queues = RabbitConfig.CONTENT_DELETE_QUEUE)
    public void handleContentDeleted(ContentDeletedEvent event) {
        log.info("Received ContentDeletedEvent from RabbitMQ: {}", event);
        deleteCurationUseCase.deleteCurationByContentId(event.contentId());
    }
}

