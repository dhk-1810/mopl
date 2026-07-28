package org.codeit.sb06.team03.mopl.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.codeit.sb06.team03.mopl.StompAuthInboundInterceptor;
import org.codeit.sb06.team03.mopl.StompContentInboundInterceptor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.CloseStatus;

/**
 * WebSocket configuration for the content module. Registers a SimpleBroker so that
 * {@code SimpMessagingTemplate} bean is created automatically. This mirrors the
 * configuration present in the dm module, but without custom interceptors.
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthInboundInterceptor stompAuthInboundInterceptor;
    private final StompContentInboundInterceptor stompContentInboundInterceptor;
    private final UserIdHandshakeInterceptor userIdHandshakeInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/contents")
                .setAllowedOriginPatterns("*")
                .addInterceptors(userIdHandshakeInterceptor)
                .withSockJS();
        registry.addEndpoint("/ws/contents")
                .setAllowedOriginPatterns("*")
                .addInterceptors(userIdHandshakeInterceptor);
    }

    /** Configure a simple broker for /sub destinations. */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub")
                .setHeartbeatValue(new long[]{4000, 4000})
                .setTaskScheduler(websocketTaskScheduler());
        registry.setApplicationDestinationPrefixes("/pub");
    }

    /** Optional transport customisation – logs connections. */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory(handler -> new WebSocketHandlerDecorator(handler) {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                log.debug("WebSocket connected: {}", session.getId());
                super.afterConnectionEstablished(session);
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                log.debug("WebSocket closed: {} - {}", session.getId(), closeStatus);
                super.afterConnectionClosed(session, closeStatus);
            }
        });
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.executor(websocketInboundExecutor())
                .interceptors(stompAuthInboundInterceptor, stompContentInboundInterceptor);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.executor(websocketOutboundExecutor());
    }

    @Bean("websocketTaskScheduler")
    public TaskScheduler websocketTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(Runtime.getRuntime().availableProcessors());
        scheduler.setThreadNamePrefix("web-socket-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(10);
        scheduler.setRemoveOnCancelPolicy(true);
        scheduler.initialize();
        return scheduler;
    }

    @Bean("taskExecutor")
    @Primary
    public org.springframework.core.task.TaskExecutor taskExecutor() {
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.setPoolSize(8);
        executor.setThreadNamePrefix("default-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    @Bean("websocketInboundExecutor")
    public TaskExecutor websocketInboundExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(16);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("inbound-");
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setKeepAliveSeconds(60);
        executor.initialize();
        return executor;
    }

    @Bean("websocketOutboundExecutor")
    public TaskExecutor websocketOutboundExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(16);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("outbound-");
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setKeepAliveSeconds(60);
        executor.initialize();
        return executor;
    }
}
