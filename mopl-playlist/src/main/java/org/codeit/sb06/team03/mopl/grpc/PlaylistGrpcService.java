package org.codeit.sb06.team03.mopl.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.codeit.sb06.team03.mopl.entity.Curation;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalContentView;
import org.codeit.sb06.team03.mopl.grpc.playlist.*;
import org.codeit.sb06.team03.mopl.repository.cqrs.ExternalContentViewRepository;
import org.codeit.sb06.team03.mopl.service.application.PlaylistCommandService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class PlaylistGrpcService extends PlaylistServiceGrpc.PlaylistServiceImplBase {

    private final PlaylistCommandService playlistCommandService;
    private final ExternalContentViewRepository externalContentViewRepository;

    @Override
    @Transactional(value = "playlistTransactionManager")
    public void deleteCurations(DeleteCurationsRequest request, StreamObserver<DeleteCurationsResponse> responseObserver) {
        try {
            UUID contentId = UUID.fromString(request.getContentId());
            log.info("[gRPC PlaylistService] DeleteCurations (Soft Delete) called for contentId: {}", contentId);

            playlistCommandService.deleteCurationByContentId(contentId);
            externalContentViewRepository.deleteById(contentId);

            DeleteCurationsResponse response = DeleteCurationsResponse.newBuilder()
                    .setSuccess(true)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("[gRPC PlaylistService] Failed to delete curations for contentId: {}", request.getContentId(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    @Transactional(value = "playlistTransactionManager")
    public void restoreCurations(RestoreCurationsRequest request, StreamObserver<RestoreCurationsResponse> responseObserver) {
        try {
            UUID contentId = UUID.fromString(request.getContentId());
            log.info("[gRPC PlaylistService] Compensating Transaction: RestoreCurations called for contentId: {}", contentId);

            playlistCommandService.restoreCurationByContentId(contentId);
            externalContentViewRepository.restoreById(contentId);
            log.info("[gRPC PlaylistService] Successfully restored curations and view for contentId: {}", contentId);

            RestoreCurationsResponse response = RestoreCurationsResponse.newBuilder()
                    .setSuccess(true)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("[gRPC PlaylistService] Failed to restore curations for contentId: {}", request.getContentId(), e);
            responseObserver.onError(e);
        }
    }
}
