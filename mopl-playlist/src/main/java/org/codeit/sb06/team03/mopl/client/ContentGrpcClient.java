package org.codeit.sb06.team03.mopl.client;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.codeit.sb06.team03.mopl.dto.response.ContentDto;
import org.codeit.sb06.team03.mopl.enums.ContentType;
import org.codeit.sb06.team03.mopl.grpc.content.ContentResponse;
import org.codeit.sb06.team03.mopl.grpc.content.ContentServiceGrpc;
import org.codeit.sb06.team03.mopl.grpc.content.GetContentRequest;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class ContentGrpcClient {

    @GrpcClient("content-service")
    private ContentServiceGrpc.ContentServiceBlockingStub contentServiceStub;

    public ContentDto getContentById(UUID contentId) {
        if (contentId == null) {
            return null;
        }
        try {
            GetContentRequest request = GetContentRequest.newBuilder()
                    .setContentId(contentId.toString())
                    .build();

            ContentResponse response = contentServiceStub.getContent(request);

            ContentType type = null;
            if (response.getType() != null && !response.getType().isBlank()) {
                try {
                    type = ContentType.valueOf(response.getType());
                } catch (Exception ignored) {}
            }

            Set<String> tags = new HashSet<>(response.getTagsList());

            return new ContentDto(
                    UUID.fromString(response.getId()),
                    type,
                    response.getTitle(),
                    response.getDescription(),
                    response.getThumbnailUrl(),
                    tags,
                    response.getAverageRating(),
                    response.getReviewCount(),
                    response.getWatcherCount()
            );
        } catch (Exception e) {
            log.warn("Failed to fetch content info via gRPC for contentId: {}, error: {}", contentId, e.getMessage());
            return null;
        }
    }
}
