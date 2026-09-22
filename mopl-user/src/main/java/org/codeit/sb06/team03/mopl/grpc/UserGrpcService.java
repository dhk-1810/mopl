package org.codeit.sb06.team03.mopl.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.codeit.sb06.team03.mopl.dto.response.UserDto;
import org.codeit.sb06.team03.mopl.grpc.user.GetUserRequest;
import org.codeit.sb06.team03.mopl.grpc.user.UserResponse;
import org.codeit.sb06.team03.mopl.grpc.user.UserServiceGrpc;
import org.codeit.sb06.team03.mopl.service.UserCompositeService;

import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final UserCompositeService userCompositeService;

    @Override
    public void getUser(GetUserRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            UUID userId = UUID.fromString(request.getUserId());
            UserDto userDto = userCompositeService.getUserDto(userId);

            UserResponse response = UserResponse.newBuilder()
                    .setId(userDto.id().toString())
                    .setEmail(userDto.email() != null ? userDto.email() : "")
                    .setName(userDto.name() != null ? userDto.name() : "")
                    .setProfileImageUrl(userDto.profileImageUrl() != null ? userDto.profileImageUrl() : "")
                    .setRole(userDto.role() != null ? userDto.role() : "")
                    .setLocked(userDto.locked() != null && userDto.locked())
                    .setCreatedAt(userDto.createdAt() != null ? userDto.createdAt().toString() : "")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid userId format: {}", request.getUserId());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid userId format").asRuntimeException());
        } catch (Exception e) {
            log.warn("Failed to get user for id: {}, error: {}", request.getUserId(), e.getMessage());
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
