package org.codeit.sb06.team03.mopl.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitConfig {

    public static final String PLAYLIST_EXCHANGE = "mopl.playlist.exchange";
    public static final String IMAGE_UPLOAD_EXCHANGE = "mopl.image.exchange";

    @Bean
    public TopicExchange playlistExchange() {
        return new TopicExchange(PLAYLIST_EXCHANGE);
    }

    @Bean
    public TopicExchange imageUploadExchange() {
        return new TopicExchange(IMAGE_UPLOAD_EXCHANGE);
    }


    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            String msgId = correlationData != null ? correlationData.getId() : "null";
            if (ack) {
                log.info("[Publisher Confirm] Broker successfully received and persisted message. id: {}", msgId);
            } else {
                log.error("[Publisher Confirm] Broker NACK/FAILED to persist message. id: {}, cause: {}", msgId, cause);
            }
        });
        rabbitTemplate.setReturnsCallback(returned -> {
            log.warn("[Publisher Returns] Message unroutable: replyCode={}, replyText={}, exchange={}, routingKey={}, message={}",
                    returned.getReplyCode(), returned.getReplyText(), returned.getExchange(), returned.getRoutingKey(), returned.getMessage());
        });
        return rabbitTemplate;
    }
}
