package org.codeit.sb06.team03.mopl.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.codeit.sb06.team03.mopl.entity.ContentReadModel;
import org.codeit.sb06.team03.mopl.grpc.content.ContentResponse;
import org.codeit.sb06.team03.mopl.grpc.content.ContentServiceGrpc;
import org.codeit.sb06.team03.mopl.grpc.content.GetContentRequest;
import org.codeit.sb06.team03.mopl.service.application.ContentQueryService;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class ContentGrpcService extends ContentServiceGrpc.ContentServiceImplBase {

    private final ContentQueryService contentQueryService;

    @Override
    @Transactional(value = "contentTransactionManager", readOnly = true)
    public void getContent(GetContentRequest request, StreamObserver<ContentResponse> responseObserver) {
        try {
            UUID contentId = UUID.fromString(request.getContentId());
            ContentReadModel content = contentQueryService.get(contentId);

            ContentResponse.Builder builder = ContentResponse.newBuilder()
                    .setId(content.id().toString())
                    .setType(content.type() != null ? content.type().name() : "")
                    .setTitle(content.title() != null ? content.title() : "")
                    .setDescription(content.description() != null ? content.description() : "")
                    .setThumbnailUrl(content.thumbnailKey() != null ? content.thumbnailKey() : "")
                    .setAverageRating(content.averageRating())
                    .setReviewCount(content.reviewCount())
                    .setWatcherCount(content.watcherCount());

            if (content.tags() != null) {
                builder.addAllTags(content.tags());
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid contentId format: {}", request.getContentId());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid contentId format").asRuntimeException());
        } catch (Exception e) {
            log.warn("Failed to get content for id: {}, error: {}", request.getContentId(), e.getMessage());
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
