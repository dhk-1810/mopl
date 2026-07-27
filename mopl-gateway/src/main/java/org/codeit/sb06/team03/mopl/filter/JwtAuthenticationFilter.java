package org.codeit.sb06.team03.mopl.filter;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JWSVerifier accessTokenVerifier;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 퍼블릭 허용 경로 목록 (인증 없이 접근 가능)
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/sign-in",
            "/api/auth/csrf-token",
            "/api/auth/refresh",
            "/api/auth/reset-password",
            "/api/users", // 회원가입 POST
            "/ws/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/favicon.svg",
            "/assets/**",
            "/index.html",
            "/"
    );

    public JwtAuthenticationFilter(
            @Value("${mopl.jwt.access-token.secret:sb06-mopl-team03-jwt-temporary-secret}")
            String accessTokenSecret
    ) throws Exception {
        this.accessTokenVerifier = new MACVerifier(accessTokenSecret.getBytes());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 퍼블릭 경로 체크
        boolean isPublicPath = isPublicPath(path, request.getMethod().name());

        // 2. 토큰 추출 (Authorization 헤더 또는 SSE용 쿼리파라미터 ?token= / ?access_token=...)
        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            try {
                SignedJWT signedJWT = SignedJWT.parse(token);

                if (signedJWT.verify(accessTokenVerifier)) {
                    JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
                    Date expiration = claimsSet.getExpirationTime();

                    if (expiration != null && expiration.after(new Date())) {
                        String userId = claimsSet.getSubject();
                        log.debug("JWT verification success for path {}. UserId: {}", path, userId);

                        // Request Header에 X-User-Id 주입
                        ServerHttpRequest modifiedRequest = request.mutate()
                                .header("X-User-Id", userId)
                                .build();

                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    } else {
                        log.warn("JWT token expired for path {}", path);
                    }
                } else {
                    log.warn("JWT signature verification failed for path {}", path);
                }
            } catch (Exception e) {
                log.warn("JWT parsing error for path {}: {}", path, e.getMessage());
            }
        } else {
            log.debug("No JWT token found for path {}", path);
        }

        // 토큰이 없거나 유효하지 않은 경우
        if (isPublicPath) {
            return chain.filter(exchange);
        }

        // 보호된 경로인데 토큰이 유효하지 않으면 401 반환
        log.warn("Unauthorized request blocked by Gateway: {} {}", request.getMethod(), path);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private boolean isPublicPath(String path, String method) {
        if ("POST".equalsIgnoreCase(method) && "/api/users".equals(path)) {
            return true; // 회원가입
        }

        return PUBLIC_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private String resolveToken(ServerHttpRequest request) {
        // Authorization Header
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // Query Parameter ?token=... or ?access_token=... (EventSource SSE 호환)
        List<String> queryParamKeys = List.of("token", "access_token", "accessToken");
        for (String key : queryParamKeys) {
            String queryToken = request.getQueryParams().getFirst(key);
            if (StringUtils.hasText(queryToken)) {
                if (queryToken.startsWith("Bearer ")) {
                    return queryToken.substring(7);
                }
                return queryToken;
            }
        }

        return null;
    }

    @Override
    public int getOrder() {
        return -1; // 최우선 필터 순서
    }
}
