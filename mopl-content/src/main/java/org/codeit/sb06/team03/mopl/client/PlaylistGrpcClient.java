package org.codeit.sb06.team03.mopl.client;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.codeit.sb06.team03.mopl.grpc.playlist.*;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PlaylistGrpcClient {

    @GrpcClient("playlist-service")
    private PlaylistServiceGrpc.PlaylistServiceBlockingStub playlistServiceStub;

    private static final long DEADLINE_SECONDS = 3;

    public void deleteCurations(UUID contentId) {
        log.info("[gRPC Client] Calling PlaylistService.DeleteCurations for contentId: {}", contentId);
        try {
            DeleteCurationsRequest request = DeleteCurationsRequest.newBuilder()
                    .setContentId(contentId.toString())
                    .build();

            DeleteCurationsResponse response = playlistServiceStub
                    .withDeadlineAfter(DEADLINE_SECONDS, TimeUnit.SECONDS)
                    .deleteCurations(request);

            if (!response.getSuccess()) {
                throw new RuntimeException("PlaylistService returned false for deleteCurations: " + contentId);
            }
            log.info("[gRPC Client] PlaylistService.DeleteCurations succeeded for contentId: {}, deletedCount: {}", 
                    contentId, response.getDeletedCount());
        } catch (StatusRuntimeException e) {
            log.error("[gRPC Client] PlaylistService.DeleteCurations failed with gRPC status: {}, contentId: {}", 
                    e.getStatus(), contentId, e);
            throw new RuntimeException("PlaylistService deleteCurations failed: " + e.getStatus().getDescription(), e);
        } catch (Exception e) {
            log.error("[gRPC Client] PlaylistService.DeleteCurations unexpected error, contentId: {}", contentId, e);
            throw new RuntimeException("PlaylistService deleteCurations error: " + e.getMessage(), e);
        }
    }

    public void restoreCurations(UUID contentId) {
        log.info("[gRPC Client] Compensating Transaction: Calling PlaylistService.RestoreCurations for contentId: {}", contentId);
        try {
            RestoreCurationsRequest request = RestoreCurationsRequest.newBuilder()
                    .setContentId(contentId.toString())
                    .build();

            RestoreCurationsResponse response = playlistServiceStub
                    .withDeadlineAfter(DEADLINE_SECONDS, TimeUnit.SECONDS)
                    .restoreCurations(request);

            if (!response.getSuccess()) {
                log.warn("[gRPC Client] PlaylistService returned false for restoreCurations: {}", contentId);
            } else {
                log.info("[gRPC Client] PlaylistService.RestoreCurations succeeded for contentId: {}", contentId);
            }
        } catch (Exception e) {
            log.error("[gRPC Client] Failed to execute compensating restoreCurations for contentId: {}", contentId, e);
        }
    }
}
