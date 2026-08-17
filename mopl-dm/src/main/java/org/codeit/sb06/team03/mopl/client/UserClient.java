package org.codeit.sb06.team03.mopl.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.UUID;

@Component
public class UserClient {

    private final RestClient restClient;

    public UserClient(@Value("${mopl.services.user.url:http://localhost:8081}") String userUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(userUrl)
                .build();
    }

    public record UserDto(
            UUID id,
            Instant createdAt,
            String email,
            String name,
            String profileImageUrl,
            String role,
            Boolean locked
    ) {}

    public UserDto getUserById(UUID userId) {
        try {
            return restClient.get()
                    .uri("/api/users/{userId}", userId)
                    .header("X-User-Id", userId.toString())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(UserDto.class);
        } catch (Exception e) {
            return null;
        }
    }
}
