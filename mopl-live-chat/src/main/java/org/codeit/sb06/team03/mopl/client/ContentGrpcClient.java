package org.codeit.sb06.team03.mopl.client;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.codeit.sb06.team03.mopl.dto.ContentSummary;
import org.codeit.sb06.team03.mopl.grpc.content.*;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class ContentGrpcClient {

    @GrpcClient("content-service")
    private ContentServiceGrpc.ContentServiceBlockingStub contentServiceStub;

    public ContentSummary getContentSummary(UUID contentId) {
        if (contentId == null) {
            return null;
        }
        try {
            GetContentRequest request = GetContentRequest.newBuilder()
                    .setContentId(contentId.toString())
                    .build();

            ContentResponse response = contentServiceStub.getContent(request);
            return new ContentSummary(
                    UUID.fromString(response.getId()),
                    response.getTitle()
            );
        } catch (Exception e) {
            log.warn("Failed to fetch content summary via gRPC for contentId: {}, error: {}", contentId, e.getMessage());
            return new ContentSummary(contentId, "Unknown Content");
        }
    }

    public int batchUpdateWatcherCounts(Map<UUID, Long> watcherCounts) {
        if (watcherCounts == null || watcherCounts.isEmpty()) {
            return 0;
        }
        try {
            BatchUpdateWatcherCountsRequest.Builder requestBuilder = BatchUpdateWatcherCountsRequest.newBuilder();
            for (Map.Entry<UUID, Long> entry : watcherCounts.entrySet()) {
                requestBuilder.addItems(WatcherCountItem.newBuilder()
                        .setContentId(entry.getKey().toString())
                        .setWatcherCount(entry.getValue())
                        .build());
            }

            BatchUpdateWatcherCountsResponse response = contentServiceStub.batchUpdateWatcherCounts(requestBuilder.build());
            return response.getUpdatedCount();
        } catch (Exception e) {
            log.warn("Failed to batch update watcher counts via gRPC, error: {}", e.getMessage());
            return 0;
        }
    }
}
