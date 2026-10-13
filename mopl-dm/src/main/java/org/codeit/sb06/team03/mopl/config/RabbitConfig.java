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

    public static final String DM_EXCHANGE = "mopl.dm.exchange";
    public static final String USER_EXCHANGE = "mopl.user.exchange";

    public static final String ROUTING_KEY_TOKEN_EXPIRED = "user.token-expired";

    public static final String TOKEN_EXPIRED_QUEUE = "dm.token-expired.queue";

    @Bean
    public Queue tokenExpiredQueue() {
        return new Queue(TOKEN_EXPIRED_QUEUE, true);
    }

    @Bean
    public Binding tokenExpiredBinding() {
        return BindingBuilder.bind(tokenExpiredQueue())
                .to(userExchange())
                .with(ROUTING_KEY_TOKEN_EXPIRED);
    }

    @Bean
    public TopicExchange dmExchange() {
        return new TopicExchange(DM_EXCHANGE);
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
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
