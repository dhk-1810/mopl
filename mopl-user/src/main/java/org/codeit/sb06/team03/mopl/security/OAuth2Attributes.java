package org.codeit.sb06.team03.mopl.security;

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
        String nickname = "";
        if (kakaoAccount != null) {
            Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");
            if (kakaoProfile != null) {
                nickname = (String) kakaoProfile.get("nickname");
            }
        }

        Object idVal = attributes.get(userNameAttributeName);
        if (idVal == null) {
            idVal = attributes.get("id");
        }
        String kakaoId = idVal != null ? String.valueOf(idVal) : "";

        // Remove non-alphanumeric characters to satisfy EmailAddress regex pattern.
        // If the resulting nickname is empty (e.g., Korean only), fallback to "kakao".
        String sanitizedNickname = nickname == null ? "" : nickname.replaceAll("[^a-zA-Z0-9]", "");
        if (sanitizedNickname.isEmpty()) {
            sanitizedNickname = "kakao";
        }

        String virtualEmail = sanitizedNickname + "_" + kakaoId + "@kakao.com";

        return new OAuth2Attributes(
                attributes,
                userNameAttributeName,
                nickname,
                virtualEmail
        );
    }

}