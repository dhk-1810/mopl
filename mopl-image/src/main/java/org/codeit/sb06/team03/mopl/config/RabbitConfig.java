package org.codeit.sb06.team03.mopl.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String IMAGE_EXCHANGE = "mopl.image.exchange";
    public static final String IMAGE_UPLOAD_ROUTING_KEY = "mopl.image.upload";
    public static final String IMAGE_UPLOAD_QUEUE = "mopl.image.queue.upload";

    @Bean
    public Queue imageUploadQueue() {
        return new Queue(IMAGE_UPLOAD_QUEUE, true);
    }

    @Bean
    public TopicExchange imageExchange() {
        return new TopicExchange(IMAGE_EXCHANGE);
    }

    @Bean
    public Binding imageUploadBinding(Queue imageUploadQueue, TopicExchange imageExchange) {
        return BindingBuilder.bind(imageUploadQueue).to(imageExchange).with(IMAGE_UPLOAD_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory) {
        org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate = new org.springframework.amqp.rabbit.core.RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
