package org.codeit.sb06.team03.mopl.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class UserHeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String userIdHeader = request.getHeader(USER_ID_HEADER);

        if (StringUtils.hasText(userIdHeader)) {
            try {
                UUID userId = UUID.fromString(userIdHeader);

                // DB 추가 조회 없이 Header에서 넘어온 userId 기반으로 Simple UserPrincipal 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (IllegalArgumentException e) {
                log.debug("Invalid X-User-Id header format: {}", userIdHeader);
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
