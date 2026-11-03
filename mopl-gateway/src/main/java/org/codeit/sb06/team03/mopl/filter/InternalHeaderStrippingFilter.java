package org.codeit.sb06.team03.mopl.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class InternalHeaderStrippingFilter implements GlobalFilter, Ordered {

    private static final List<String> RESTRICTED_PREFIXES = List.of("x-user-", "x-internal-");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        List<String> headersToRemove = request.getHeaders().keySet().stream()
                .filter(this::isRestrictedHeader)
                .toList();

        if (!headersToRemove.isEmpty()) {
            log.warn("Stripped restricted headers {} from request path: {}", headersToRemove, request.getURI().getPath());

            ServerHttpRequest sanitizedRequest = request.mutate()
                    .headers(headers -> headersToRemove.forEach(headers::remove))
                    .build();

            return chain.filter(exchange.mutate().request(sanitizedRequest).build());
        }

        return chain.filter(exchange);
    }

    private boolean isRestrictedHeader(String headerName) {
        String lower = headerName.toLowerCase();
        return RESTRICTED_PREFIXES.stream().anyMatch(lower::startsWith);
    }

    @Override
    public int getOrder() {
        return -2; // JwtAuthenticationFilter(order: -1)보다 먼저 실행
    }
}
