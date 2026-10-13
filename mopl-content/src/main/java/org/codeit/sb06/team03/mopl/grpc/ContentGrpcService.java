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
import org.codeit.sb06.team03.mopl.dto.request.ContentCreateInternalRequest;
import org.codeit.sb06.team03.mopl.enums.ContentType;
import org.codeit.sb06.team03.mopl.grpc.content.BatchCreateContentsRequest;
import org.codeit.sb06.team03.mopl.grpc.content.BatchCreateContentsResponse;
import org.codeit.sb06.team03.mopl.grpc.content.CreateContentItem;
import org.codeit.sb06.team03.mopl.service.application.ContentCommandService;
import org.codeit.sb06.team03.mopl.service.application.ContentQueryService;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class ContentGrpcService extends ContentServiceGrpc.ContentServiceImplBase {

    private final ContentQueryService contentQueryService;
    private final ContentCommandService contentCommandService;

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

    @Override
    @Transactional(value = "contentTransactionManager", readOnly = true)
    public void checkContentExists(
            org.codeit.sb06.team03.mopl.grpc.content.CheckContentExistsRequest request,
            StreamObserver<org.codeit.sb06.team03.mopl.grpc.content.CheckContentExistsResponse> responseObserver
    ) {
        try {
            org.codeit.sb06.team03.mopl.enums.ContentType type = null;
            if (request.getType() != null && !request.getType().isBlank()) {
                type = org.codeit.sb06.team03.mopl.enums.ContentType.valueOf(request.getType());
            }
            boolean exists = contentQueryService.existsByTitleAndType(request.getTitle(), type);

            org.codeit.sb06.team03.mopl.grpc.content.CheckContentExistsResponse response =
                    org.codeit.sb06.team03.mopl.grpc.content.CheckContentExistsResponse.newBuilder()
                            .setExists(exists)
                            .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid contentType format: {}", request.getType());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid contentType: " + request.getType()).asRuntimeException());
        } catch (Exception e) {
            log.error("Failed to check content exists for title: {}, error: {}", request.getTitle(), e.getMessage());
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void batchCreateContents(
            BatchCreateContentsRequest request,
            StreamObserver<BatchCreateContentsResponse> responseObserver
    ) {
        try {
            int createdCount = 0;
            for (CreateContentItem item : request.getContentsList()) {
                ContentType type = ContentType.valueOf(item.getType());
                Set<String> tags = new HashSet<>(item.getTagsList());
                ContentCreateInternalRequest createRequest = new ContentCreateInternalRequest(
                        type,
                        item.getTitle(),
                        item.getDescription(),
                        tags,
                        item.getThumbnailKey()
                );
                contentCommandService.createInternal(createRequest);
                createdCount++;
            }

            BatchCreateContentsResponse response = BatchCreateContentsResponse.newBuilder()
                    .setCreatedCount(createdCount)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid contentType format in batch create: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            log.error("Failed to batch create contents: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
}
