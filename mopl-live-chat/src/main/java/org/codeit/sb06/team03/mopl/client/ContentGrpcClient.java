package org.codeit.sb06.team03.mopl.client;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.codeit.sb06.team03.mopl.dto.ContentSummary;
import org.codeit.sb06.team03.mopl.grpc.content.ContentResponse;
import org.codeit.sb06.team03.mopl.grpc.content.ContentServiceGrpc;
import org.codeit.sb06.team03.mopl.grpc.content.GetContentRequest;
import org.springframework.stereotype.Component;

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
}
