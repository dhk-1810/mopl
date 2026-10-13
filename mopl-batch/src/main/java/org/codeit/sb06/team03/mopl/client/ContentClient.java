package org.codeit.sb06.team03.mopl.client;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.codeit.sb06.team03.mopl.grpc.content.CheckContentExistsRequest;
import org.codeit.sb06.team03.mopl.grpc.content.CheckContentExistsResponse;
import org.codeit.sb06.team03.mopl.grpc.content.ContentServiceGrpc;
import org.springframework.stereotype.Component;

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
}
