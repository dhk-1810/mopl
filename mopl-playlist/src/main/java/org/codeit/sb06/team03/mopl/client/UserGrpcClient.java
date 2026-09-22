package org.codeit.sb06.team03.mopl.client;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.codeit.sb06.team03.mopl.grpc.user.GetUserRequest;
import org.codeit.sb06.team03.mopl.grpc.user.UserResponse;
import org.codeit.sb06.team03.mopl.grpc.user.UserServiceGrpc;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
public class UserGrpcClient {

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

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
        if (userId == null) {
            return null;
        }
        try {
            GetUserRequest request = GetUserRequest.newBuilder()
                    .setUserId(userId.toString())
                    .build();

            UserResponse response = userServiceStub.getUser(request);

            Instant createdAt = null;
            if (response.getCreatedAt() != null && !response.getCreatedAt().isBlank()) {
                try {
                    createdAt = Instant.parse(response.getCreatedAt());
                } catch (Exception ignored) {}
            }

            return new UserDto(
                    UUID.fromString(response.getId()),
                    createdAt,
                    response.getEmail(),
                    response.getName(),
                    response.getProfileImageUrl(),
                    response.getRole(),
                    response.getLocked()
            );
        } catch (Exception e) {
            log.warn("Failed to fetch user info via gRPC for userId: {}, error: {}", userId, e.getMessage());
            return null;
        }
    }
}
