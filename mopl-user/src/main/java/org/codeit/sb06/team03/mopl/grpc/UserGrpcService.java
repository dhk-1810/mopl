package org.codeit.sb06.team03.mopl.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.codeit.sb06.team03.mopl.entity.Account;
import org.codeit.sb06.team03.mopl.grpc.user.GetUserRequest;
import org.codeit.sb06.team03.mopl.grpc.user.UserResponse;
import org.codeit.sb06.team03.mopl.grpc.user.UserServiceGrpc;
import org.codeit.sb06.team03.mopl.service.AccountQueryService;

import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final AccountQueryService accountQueryService;

    @Override
    public void getUser(GetUserRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            UUID userId = UUID.fromString(request.getUserId());
            Account account = accountQueryService.getById(userId);
            String profileImageKey = (account.getProfile() != null && account.getProfile().getImageKey() != null)
                    ? account.getProfile().getImageKey() : "";
            String name = (account.getProfile() != null && account.getProfile().getName() != null)
                    ? account.getProfile().getName() : "";

            UserResponse response = UserResponse.newBuilder()
                    .setId(account.getId().toString())
                    .setEmail(account.getEmailAddress() != null ? account.getEmailAddress().value() : "")
                    .setName(name)
                    .setProfileImageKey(profileImageKey)
                    .setRole(account.getRole() != null ? account.getRole().name() : "")
                    .setLocked(account.isLocked())
                    .setCreatedAt(account.getCreatedAt() != null ? account.getCreatedAt().toString() : "")
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
