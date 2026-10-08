package org.codeit.sb06.team03.mopl.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.codeit.sb06.team03.mopl.grpc.watchingsession.DeleteWatchingSessionsRequest;
import org.codeit.sb06.team03.mopl.grpc.watchingsession.DeleteWatchingSessionsResponse;
import org.codeit.sb06.team03.mopl.grpc.watchingsession.WatchingSessionServiceGrpc;
import org.codeit.sb06.team03.mopl.service.application.WatchingSessionCommandService;

import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class WatchingSessionGrpcService extends WatchingSessionServiceGrpc.WatchingSessionServiceImplBase {

    private final WatchingSessionCommandService watchingSessionCommandService;

    @Override
    public void deleteWatchingSessions(DeleteWatchingSessionsRequest request, StreamObserver<DeleteWatchingSessionsResponse> responseObserver) {
        try {
            UUID contentId = UUID.fromString(request.getContentId());
            log.info("[gRPC WatchingSessionService] DeleteWatchingSessions called for contentId: {}", contentId);

            // 해당 contentId와 연관된 시청 세션 정리
            watchingSessionCommandService.deleteByLiveChatRoomId(contentId);

            DeleteWatchingSessionsResponse response = DeleteWatchingSessionsResponse.newBuilder()
                    .setSuccess(true)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("[gRPC WatchingSessionService] Failed to delete watching sessions for contentId: {}", request.getContentId(), e);
            responseObserver.onError(e);
        }
    }
}
