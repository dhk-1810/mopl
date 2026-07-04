package org.codeit.sb06.team03.mopl.auth;

import org.codeit.sb06.team03.mopl.account.domain.Account;
import org.codeit.sb06.team03.mopl.account.domain.vo.Role;
import org.springframework.security.core.userdetails.User;

import java.util.Map;

public record OAuth2Attributes(
        Map<String, Object> attributes,
        String nameAttributeKey,
        String name,
        String email
) {
    public static OAuth2Attributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("kakao".equals(registrationId)) {
            return ofKakao(userNameAttributeName, attributes);
        }
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuth2Attributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return new OAuth2Attributes(
                attributes,
                userNameAttributeName,
                (String) attributes.get("name"),
                (String) attributes.get("email")
        );
    }

    private static OAuth2Attributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");

        return new OAuth2Attributes(
                attributes,
                userNameAttributeName,
                (String) kakaoProfile.get("nickname"),
                (String) kakaoAccount.get("email")
        );
    }

    public SocialLoginCommand toCommand(String registrationId) {
        return new SocialLoginCommand(registrationId, name, email);
    }
}