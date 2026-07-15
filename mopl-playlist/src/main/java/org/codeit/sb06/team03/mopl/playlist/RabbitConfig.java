package org.codeit.sb06.team03.mopl.playlist;

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
public class RabbitConfig { // RabbitMQ는 AMQP의 구현체 중 하나임. (AMQP: 서버 모듈끼리 메시지를 안전하게 비동기로 주고받기 위한 표준 프로토콜)

    // Exchange: publisher가 보낸 메시지를 받아 어떤 Queue로 보낼지 결정함.
    public static final String PLAYLIST_EXCHANGE = "mopl.playlist.exchange";
    public static final String CONTENT_EXCHANGE = "mopl.content.exchange";

    // Routing Key: Exchange(교환기)가 이 메시지를 어떤 Queue(우편함)로 보내야 할지 결정하는 기준
    public static final String ROUTING_KEY_CONTENT_DELETED = "content.deleted";

    public static final String CONTENT_DELETE_QUEUE = "playlist.content-delete.queue";

    // TopicExchange는 라우팅 키의 패턴(와일드카드)을 매칭해서 메시지를 전달해 주는 Exchange임. (예: playlist.*  playlist.# )
    @Bean
    public TopicExchange playlistExchange() {
        return new TopicExchange(PLAYLIST_EXCHANGE);
    }
    @Bean
    public TopicExchange contentExchange() {
        return new TopicExchange(CONTENT_EXCHANGE);
    }

    @Bean
    public Queue contentDeleteQueue() {
        return new Queue(CONTENT_DELETE_QUEUE, true);
    }

    @Bean
    public Binding contentDeleteBinding() {
        return BindingBuilder.bind(contentDeleteQueue())
                .to(contentExchange())
                .with(ROUTING_KEY_CONTENT_DELETED);
    }

    @Bean // MessageConverter : JSON 직렬화, 역직렬화
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
