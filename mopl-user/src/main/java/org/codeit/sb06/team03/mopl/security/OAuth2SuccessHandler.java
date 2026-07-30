package org.codeit.sb06.team03.mopl.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dto.response.UserDto;
import org.codeit.sb06.team03.mopl.security.jwt.JwtClaims;
import org.codeit.sb06.team03.mopl.security.jwt.RefreshTokenCookieProvider;
import org.codeit.sb06.team03.mopl.security.jwt.TokenPair;
import org.codeit.sb06.team03.mopl.security.jwt.registry.JwtRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${mopl.oauth2.redirect-uri.success}")
    private String loginSuccessRedirectUri;

    @Value("${mopl.oauth2.redirect-uri.failure}")
    private String loginFailureRedirectUri;

    @Value("${mopl.jwt.refresh-token.expiration-ms}")
    private long refreshTokenExpirationInMs;

    private final JwtRegistry jwtRegistry;
    private final RefreshTokenCookieProvider refreshTokenCookieProvider;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        if (authentication.getPrincipal() instanceof MoplUserDetails userDetails) {
            try {
                JwtClaims jwtClaims = userDetailsToJwtClaims(userDetails);
                TokenPair tokenPair = jwtRegistry.register(jwtClaims);

                ResponseCookie refreshTokenCookie = ResponseCookie.from("REFRESH_TOKEN", tokenPair.refreshToken())
                        .path("/")
                        .httpOnly(true)
                        .maxAge(refreshTokenExpirationInMs / 1000)
                        .sameSite("Lax") // 로컬 개발 리다이렉트 대응
                        .build();

                response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

                // Access Token을 파라미터로 실어서 프론트엔드로 리다이렉트
                String targetUrl = UriComponentsBuilder.fromUriString(loginSuccessRedirectUri)
                        .queryParam("accessToken", tokenPair.accessToken())
                        .build().toUriString();

                getRedirectStrategy().sendRedirect(request, response, targetUrl);
                return;
            } catch (Exception e) {
                // 토큰 생성 실패 시 프론트엔드 로그인 페이지로 에러와 함께 리다이렉트
                String errorUrl = UriComponentsBuilder.fromUriString(loginFailureRedirectUri)
                        .queryParam("error", "token_generation_failed")
                        .build().toUriString();
                getRedirectStrategy().sendRedirect(request, response, errorUrl);
                return;
            }
        }

        String unauthorizedUrl = UriComponentsBuilder.fromUriString(loginFailureRedirectUri)
                .queryParam("error", "unauthorized")
                .build().toUriString();
        getRedirectStrategy().sendRedirect(request, response, unauthorizedUrl);
    }

    private JwtClaims userDetailsToJwtClaims(MoplUserDetails moplUserDetails) {
        UserDto userDto = moplUserDetails.getUserDto();
        return new JwtClaims(
                userDto.id(),
                userDto.email(),
                userDto.name(),
                userDto.profileImageUrl(),
                userDto.role()
        );
    }
}
