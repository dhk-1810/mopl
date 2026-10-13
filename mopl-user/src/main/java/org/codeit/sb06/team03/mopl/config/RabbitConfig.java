package org.codeit.sb06.team03.mopl.config;

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
    public static final String USER_ROLE_UPDATED_ROUTING_KEY = "mopl.user.role-updated";
    public static final String USER_FOLLOWED_ROUTING_KEY = "mopl.user.followed";

    public static final String IMAGE_EXCHANGE = "mopl.image.exchange";
    public static final String IMAGE_UPLOAD_ROUTING_KEY = "mopl.image.upload";

    public static final String WATCHING_SESSION_EXCHANGE = "mopl.watching-session.exchange";
    public static final String WATCHING_SESSION_DELETE_ROUTING_KEY = "mopl.watching-session.delete";

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    @Bean
    public TopicExchange imageExchange() {
        return new TopicExchange(IMAGE_EXCHANGE);
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
