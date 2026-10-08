package org.codeit.sb06.team03.mopl.client;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.codeit.sb06.team03.mopl.grpc.watchingsession.DeleteWatchingSessionsRequest;
import org.codeit.sb06.team03.mopl.grpc.watchingsession.DeleteWatchingSessionsResponse;
import org.codeit.sb06.team03.mopl.grpc.watchingsession.WatchingSessionServiceGrpc;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class WatchingSessionGrpcClient {

    @GrpcClient("watching-session-service")
    private WatchingSessionServiceGrpc.WatchingSessionServiceBlockingStub watchingSessionServiceStub;

    private static final long DEADLINE_SECONDS = 3;

    public void deleteWatchingSessions(UUID contentId) {
        log.info("[gRPC Client] Calling WatchingSessionService.DeleteWatchingSessions for contentId: {}", contentId);
        try {
            DeleteWatchingSessionsRequest request = DeleteWatchingSessionsRequest.newBuilder()
                    .setContentId(contentId.toString())
                    .build();

            DeleteWatchingSessionsResponse response = watchingSessionServiceStub
                    .withDeadlineAfter(DEADLINE_SECONDS, TimeUnit.SECONDS)
                    .deleteWatchingSessions(request);

            if (!response.getSuccess()) {
                throw new RuntimeException("WatchingSessionService returned false for deleteWatchingSessions: " + contentId);
            }
            log.info("[gRPC Client] WatchingSessionService.DeleteWatchingSessions succeeded for contentId: {}", contentId);
        } catch (StatusRuntimeException e) {
            log.error("[gRPC Client] WatchingSessionService.DeleteWatchingSessions failed with gRPC status: {}, contentId: {}", 
                    e.getStatus(), contentId, e);
            throw new RuntimeException("WatchingSessionService deleteWatchingSessions failed: " + e.getStatus().getDescription(), e);
        } catch (Exception e) {
            log.error("[gRPC Client] WatchingSessionService.DeleteWatchingSessions unexpected error, contentId: {}", contentId, e);
            throw new RuntimeException("WatchingSessionService deleteWatchingSessions error: " + e.getMessage(), e);
        }
    }
}
