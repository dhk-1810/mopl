package org.codeit.sb06.team03.mopl.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String USER_EXCHANGE = "mopl.user.exchange";
    public static final String IMAGE_UPLOAD_QUEUE = "mopl.image.queue.upload";
    public static final String IMAGE_UPLOAD_EXCHANGE = "mopl.image.exchange";
    public static final String IMAGE_UPLOAD_ROUTING_KEY = "mopl.image.upload";

    public static final String IMAGE_PRESIGNED_URL_CREATED_QUEUE = "user.image-presigned-url-created.queue";
    public static final String IMAGE_PRESIGNED_URL_CREATED_ROUTING_KEY = "mopl.image.presigned-url-created";

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    @Bean
    public Queue imageUploadQueue() {
        return new Queue(IMAGE_UPLOAD_QUEUE, true);
    }

    @Bean
    public TopicExchange imageUploadExchange() {
        return new TopicExchange(IMAGE_UPLOAD_EXCHANGE);
    }

    @Bean
    public Binding imageUploadBinding(Queue imageUploadQueue, TopicExchange imageUploadExchange) {
        return BindingBuilder.bind(imageUploadQueue).to(imageUploadExchange).with(IMAGE_UPLOAD_ROUTING_KEY);
    }

    @Bean
    public Queue imagePresignedUrlCreatedQueue() {
        return new Queue(IMAGE_PRESIGNED_URL_CREATED_QUEUE, true);
    }

    @Bean
    public Binding imagePresignedUrlCreatedBinding(Queue imagePresignedUrlCreatedQueue, TopicExchange imageUploadExchange) {
        return BindingBuilder.bind(imagePresignedUrlCreatedQueue).to(imageUploadExchange).with(IMAGE_PRESIGNED_URL_CREATED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        return rabbitTemplate;
    }
}
