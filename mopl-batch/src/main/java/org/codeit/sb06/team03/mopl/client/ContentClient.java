package org.codeit.sb06.team03.mopl.client;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.codeit.sb06.team03.mopl.dto.ContentCreateRequest;
import org.codeit.sb06.team03.mopl.grpc.content.BatchCreateContentsRequest;
import org.codeit.sb06.team03.mopl.grpc.content.BatchCreateContentsResponse;
import org.codeit.sb06.team03.mopl.grpc.content.CreateContentItem;
import org.codeit.sb06.team03.mopl.grpc.content.CheckContentExistsRequest;
import org.codeit.sb06.team03.mopl.grpc.content.CheckContentExistsResponse;
import org.codeit.sb06.team03.mopl.grpc.content.ContentServiceGrpc;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ContentClient {

    @GrpcClient("content-service")
    private ContentServiceGrpc.ContentServiceBlockingStub contentServiceStub;

    public boolean exists(String title, String type) {
        try {
            CheckContentExistsRequest request = CheckContentExistsRequest.newBuilder()
                    .setTitle(title != null ? title : "")
                    .setType(type != null ? type : "")
                    .build();

            CheckContentExistsResponse response = contentServiceStub.checkContentExists(request);
            return response.getExists();
        } catch (Exception e) {
            log.error("Failed to check content existence via gRPC for title: {}, type: {}, error: {}",
                    title, type, e.getMessage());
            return false;
        }
    }

    public int batchCreateContents(List<? extends ContentCreateRequest> items) {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        try {
            BatchCreateContentsRequest.Builder requestBuilder = BatchCreateContentsRequest.newBuilder();
            for (ContentCreateRequest item : items) {
                CreateContentItem.Builder itemBuilder = CreateContentItem.newBuilder()
                        .setType(item.type() != null ? item.type().name() : "")
                        .setTitle(item.title() != null ? item.title() : "")
                        .setDescription(item.description() != null ? item.description() : "")
                        .setThumbnailKey(item.thumbnailKey() != null ? item.thumbnailKey() : "");
                if (item.tags() != null) {
                    itemBuilder.addAllTags(item.tags());
                }
                requestBuilder.addContents(itemBuilder.build());
            }

            BatchCreateContentsResponse response = contentServiceStub.batchCreateContents(requestBuilder.build());
            return response.getCreatedCount();
        } catch (Exception e) {
            log.error("Failed to batch create contents via gRPC: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to batch create contents via gRPC", e);
        }
    }
}
