package org.codeit.sb06.team03.mopl.controller;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dto.request.ResetPasswordRequest;
import org.codeit.sb06.team03.mopl.service.AuthCompositeService;
import org.codeit.sb06.team03.mopl.security.jwt.*;
import org.codeit.sb06.team03.mopl.security.jwt.exception.InvalidTokenException;
import org.codeit.sb06.team03.mopl.security.jwt.registry.JwtRegistry;
import org.codeit.sb06.team03.mopl.dto.response.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

    private final AuthCompositeService authCompositeService;
    private final RefreshTokenCookieProvider cookieProvider;
    private final JwtRegistry jwtRegistry;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authCompositeService.resetPassword(request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @GetMapping("/csrf-token")
    public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<JwtDto> refresh(HttpServletRequest request, HttpServletResponse response) {
        Cookie oldRefreshTokenCookie =  cookieProvider.resolveCookie(request);
        if (oldRefreshTokenCookie == null) {
            throw new InvalidTokenException();
        }

        TokenPair tokenPair = jwtRegistry.rotate(oldRefreshTokenCookie.getValue());

        Cookie newRefreshTokenCookie = cookieProvider.generateRefreshTokenCookie(tokenPair.refreshToken());
        response.addCookie(newRefreshTokenCookie);

        JwtClaims jwtClaims = jwtTokenProvider.getClaims(tokenPair.refreshToken());
        UUID accountId = jwtClaims.id();
        UserDto userDto = authCompositeService.getUserDto(accountId);
        JwtDto jwtDto = new JwtDto(
                userDto, tokenPair.accessToken()
        );

        return ResponseEntity.ok(jwtDto);
    }

    @Override
    @PostMapping("/sign-in")
    public ResponseEntity<JwtDto> login(
            @RequestParam(name = "username") String username,
            @RequestParam(name = "password") String password) {
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    @PostMapping("/sign-out")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @GetMapping("/validate")
    public ResponseEntity<Void> validate(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(name = "strict", defaultValue = "true") boolean strict
    ) {
        String bearerToken = request.getHeader("Authorization");
        if (org.springframework.util.StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String accessToken = bearerToken.substring(7);
            if (jwtRegistry.hasActiveAccessToken(accessToken)) {
                JwtClaims claims = jwtTokenProvider.getClaims(accessToken);
                response.setHeader("X-User-Id", claims.id().toString());
                return ResponseEntity.ok().build();
            }
        }

        if (strict) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok().build();
    }
}
