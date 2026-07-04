package org.codeit.sb06.team03.mopl.auth;

public record SocialLoginCommand(
        String registrationId, // 소셜로그인 제공자
        String name,
        String email
) {
}
