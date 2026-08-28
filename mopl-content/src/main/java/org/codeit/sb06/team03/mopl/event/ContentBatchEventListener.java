package org.codeit.sb06.team03.mopl.event;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.dto.request.ContentCreateInternalRequest;
import org.codeit.sb06.team03.mopl.service.composite.ContentCompositeService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentBatchEventListener {

    private final ContentCompositeService contentCompositeService;

    @RabbitListener(queues = RabbitConfig.CONTENT_BATCH_INFO_QUEUE)
    public void handleContentBatchInfo(
            ContentCreateInternalRequest request,
            Channel channel, // RabbitMQ 통신 파이프라인. basicAck, basicReject, basicNack 등 호출하는 주체.
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag // 메시지 고유 식별자. (몇 번 메시지에 대한 응답인지)
    ) throws IOException {
        log.info("Received content batch message from RabbitMQ: title={}", request.title());
        try {
            contentCompositeService.createInternal(request);
            log.info("Successfully created content from batch message: title={}", request.title());
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to process content batch message: title={}", request.title(), e);
            channel.basicReject(deliveryTag, false);
        }
    }
}
