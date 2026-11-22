package org.codeit.sb06.team03.mopl.filter;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
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

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;
    private final JWSVerifier macVerifier;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 퍼블릭 허용 경로 목록 (인증 없이 접근 가능)
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/sign-in",
            "/api/auth/csrf-token",
            "/api/auth/refresh",
            "/api/auth/reset-password",
            "/api/users", // 회원가입 POST
            "/oauth2/**",
            "/login/oauth2/**",
            "/oauth-redirect",
            "/realms/**",
            "/resources/**",
            "/ws/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/favicon.svg",
            "/assets/**",
            "/index.html",
            "/"
    );

    public JwtAuthenticationFilter(
            @Value("${mopl.jwt.jwk-set-uri:}") String jwkSetUri,
            @Value("${mopl.jwt.access-token.secret:}") String accessTokenSecret
    ) {
        // 1. Keycloak OIDC 비대칭키(RS256) JWKS 프로세서 설정
        if (StringUtils.hasText(jwkSetUri)) {
            ConfigurableJWTProcessor<SecurityContext> processor = null;
            try {
                processor = new DefaultJWTProcessor<>();
                JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URI(jwkSetUri).toURL());
                JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource);
                processor.setJWSKeySelector(keySelector);
                log.info("Initialized Keycloak JWKS verification with URI: {}", jwkSetUri);
            } catch (Exception e) {
                log.warn("Failed to initialize RemoteJWKSet for URI: {}. RS256 verification may fail.", jwkSetUri, e);
                processor = null;
            }
            this.jwtProcessor = processor;
        } else {
            this.jwtProcessor = null;
        }

        // 2. 기존 대칭키(HS256) MACVerifier 설정 (하위 호환성)
        if (StringUtils.hasText(accessTokenSecret)) {
            JWSVerifier verifier = null;
            try {
                verifier = new MACVerifier(accessTokenSecret.getBytes());
            } catch (Exception e) {
                log.warn("Failed to initialize MACVerifier with provided secret", e);
            }
            this.macVerifier = verifier;
        } else {
            this.macVerifier = null;
        }
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
                JWSAlgorithm algorithm = signedJWT.getHeader().getAlgorithm();
                JWTClaimsSet claimsSet = null;

                if (JWSAlgorithm.RS256.equals(algorithm)) {
                    if (jwtProcessor != null) {
                        claimsSet = jwtProcessor.process(signedJWT, null);
                    } else {
                        log.warn("RS256 token received but Keycloak JWKS processor is not configured for path {}", path);
                    }
                } else if (JWSAlgorithm.HS256.equals(algorithm)) {
                    if (macVerifier != null && signedJWT.verify(macVerifier)) {
                        claimsSet = signedJWT.getJWTClaimsSet();
                    } else {
                        log.warn("HS256 signature verification failed for path {}", path);
                    }
                } else {
                    log.warn("Disallowed JWT algorithm detected: {} for path {}", algorithm, path);
                }

                if (claimsSet != null) {
                    Date expiration = claimsSet.getExpirationTime();

                    if (expiration != null && expiration.after(new Date())) {
                        String userId = claimsSet.getSubject();
                        String role = extractRole(claimsSet);
                        log.debug("JWT verification success for path {}. UserId: {}, Role: {}", path, userId, role);

                        // Request Header에 X-User-Id 및 X-User-Role 주입
                        ServerHttpRequest.Builder requestBuilder = request.mutate()
                                .header("X-User-Id", userId);
                        if (StringUtils.hasText(role)) {
                            requestBuilder.header("X-User-Role", role);
                        }

                        return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
                    } else {
                        log.warn("JWT token expired for path {}", path);
                    }
                }
            } catch (Exception e) {
                log.warn("JWT verification error for path {}: {}", path, e.getMessage());
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

    private String extractRole(JWTClaimsSet claimsSet) {
        try {
            String role = claimsSet.getStringClaim("role");
            if (StringUtils.hasText(role)) {
                return role;
            }

            // Keycloak 기본 형식: realm_access.roles
            Map<String, Object> realmAccess = claimsSet.getJSONObjectClaim("realm_access");
            if (realmAccess != null && realmAccess.get("roles") instanceof List<?> roles) {
                if (roles.contains("ADMIN")) {
                    return "ROLE_ADMIN";
                }
                if (roles.contains("USER")) {
                    return "ROLE_USER";
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract role from JWT claims: {}", e.getMessage());
        }
        return null;
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

        // Query Parameter ?token=... or ?access_token=... (EventSource SSE / SockJS 호환)
        List<String> tokenKeys = List.of("token", "access_token", "accessToken");
        for (String key : tokenKeys) {
            String queryToken = request.getQueryParams().getFirst(key);
            if (StringUtils.hasText(queryToken)) {
                if (queryToken.startsWith("Bearer ")) {
                    return queryToken.substring(7);
                }
                return queryToken;
            }
        }

        // Cookie에서 access_token / accessToken / token 추출 (SockJS / Browser Request 호환)
        for (String key : tokenKeys) {
            org.springframework.http.HttpCookie cookie = request.getCookies().getFirst(key);
            if (cookie != null && StringUtils.hasText(cookie.getValue())) {
                String val = cookie.getValue();
                if (val.startsWith("Bearer ")) {
                    return val.substring(7);
                }
                return val;
            }
        }

        return null;
    }

    @Override
    public int getOrder() {
        return -1; // 최우선 필터 순서
    }
}
