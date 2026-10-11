package org.codeit.sb06.team03.mopl.service.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.client.PlaylistGrpcClient;
import org.codeit.sb06.team03.mopl.client.WatchingSessionGrpcClient;
import org.codeit.sb06.team03.mopl.service.application.ContentCommandService;
import org.codeit.sb06.team03.mopl.service.application.ContentQueryService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mopl.saga.type", havingValue = "grpc", matchIfMissing = true)
public class GrpcContentDeletionSagaStrategy implements ContentDeletionSagaStrategy {

    private final ContentQueryService contentQueryService;
    private final ContentCommandService contentCommandService;
    private final PlaylistGrpcClient playlistGrpcClient;
    private final WatchingSessionGrpcClient watchingSessionGrpcClient;

    @Override
    public void deleteContent(UUID contentId) {
        // 1. 대상 컨텐츠 존재 여부 사전 확인 (없으면 ContentNotFoundException 발생)
        contentQueryService.get(contentId);

        boolean playlistDeleted = false;
        try {
            // Step 1: Playlist gRPC 호출 (Curation 및 View 삭제)
            playlistGrpcClient.deleteCurations(contentId);
            playlistDeleted = true;

            // Step 2: LiveChat gRPC 호출 (WatchingSession 정리)
            watchingSessionGrpcClient.deleteWatchingSessions(contentId);

            // Step 3: 외부 마이크로서비스 정리 성공 후 로컬 DB 삭제 마킹 (0.001초 짧은 트랜잭션)
            contentCommandService.delete(contentId);
            log.info("[Saga Strategy: gRPC] Successfully deleted content via sequential gRPC orchestration: {}", contentId);

        } catch (Exception e) {
            log.error("[Saga Strategy: gRPC] Failed to delete content: {}. Triggering compensation...", contentId, e);

            // 보상 트랜잭션: Step 1 성공 후 Step 2 실패 시 롤백 수행
            if (playlistDeleted) {
                playlistGrpcClient.restoreCurations(contentId);
            }

            throw new RuntimeException("Content deletion failed and compensated: " + e.getMessage(), e);
        }
    }
}
