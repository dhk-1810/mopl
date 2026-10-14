package org.codeit.sb06.team03.mopl.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.codeit.sb06.team03.mopl.config.KafkaConfig;
import org.codeit.sb06.team03.mopl.entity.TimeoutImage;
import org.codeit.sb06.team03.mopl.entity.policy.PresignedUrlTimeoutPolicy;
import org.codeit.sb06.team03.mopl.event.ImagePresignedUrlKafkaEvent;
import org.codeit.sb06.team03.mopl.grpc.image.GetPresignedUrlRequest;
import org.codeit.sb06.team03.mopl.grpc.image.GetPresignedUrlResponse;
import org.codeit.sb06.team03.mopl.grpc.image.ImageServiceGrpc;
import org.codeit.sb06.team03.mopl.repository.ImageRepository;
import org.codeit.sb06.team03.mopl.service.S3Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@GrpcService
public class ImageGrpcService extends ImageServiceGrpc.ImageServiceImplBase {

    private final ImageRepository imageRepository;
    private final S3Service s3Service;
    private final PresignedUrlTimeoutPolicy presignedUrlTimeoutPolicy;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ImageGrpcService(
            ImageRepository imageRepository,
            S3Service s3Service,
            @Qualifier("imageBasicPresignedUrlTimeoutPolicy") PresignedUrlTimeoutPolicy presignedUrlTimeoutPolicy,
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.imageRepository = imageRepository;
        this.s3Service = s3Service;
        this.presignedUrlTimeoutPolicy = presignedUrlTimeoutPolicy;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    @Transactional("imageTransactionManager")
    public void getPresignedUrl(GetPresignedUrlRequest request, StreamObserver<GetPresignedUrlResponse> responseObserver) {
        String key = request.getKey();
        if (key == null || key.isBlank()) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Image key must not be blank").asRuntimeException());
            return;
        }

        try {
            // 외부 URL인 경우 그대로 반환
            if (key.startsWith("http://") || key.startsWith("https://")) {
                GetPresignedUrlResponse response = GetPresignedUrlResponse.newBuilder()
                        .setKey(key)
                        .setPresignedUrl(key)
                        .setExp("")
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            Optional<TimeoutImage> optionalImage = imageRepository.findByKey(key);
            String presignedUrl;
            Instant exp;

            if (optionalImage.isPresent() && !optionalImage.get().isExpired()) {
                TimeoutImage existing = optionalImage.get();
                presignedUrl = existing.getPresignedUrl();
                exp = existing.getExp();
            } else {
                // 만료되었거나 DB에 캐시가 없는 경우 S3에서 신규 Presigned URL 생성 (Lazy 갱신)
                exp = presignedUrlTimeoutPolicy.createExp(Instant.now());
                presignedUrl = s3Service.createPresignedUrl(key, presignedUrlTimeoutPolicy.timeout());

                if (optionalImage.isPresent()) {
                    TimeoutImage existing = optionalImage.get();
                    existing.updatePresignedUrl(exp, presignedUrl);
                    imageRepository.save(existing);
                } else {
                    TimeoutImage newImg = TimeoutImage.create(key, exp, presignedUrl);
                    imageRepository.save(newImg);
                }

                // 타 서비스 복제 뷰 동기화를 위해 Kafka 이벤트 브로드캐스팅
                ImagePresignedUrlKafkaEvent urlEvent = new ImagePresignedUrlKafkaEvent(key, presignedUrl, exp);
                kafkaTemplate.send(KafkaConfig.TOPIC_IMAGE_PRESIGNED_URL_EVENTS, key, urlEvent);
                log.info("Lazy renewed Presigned URL for key: {}", key);
            }

            GetPresignedUrlResponse response = GetPresignedUrlResponse.newBuilder()
                    .setKey(key)
                    .setPresignedUrl(presignedUrl)
                    .setExp(exp != null ? exp.toString() : "")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to get/renew Presigned URL for key: {}", key, e);
            responseObserver.onError(Status.INTERNAL.withDescription("Failed to get presigned URL: " + e.getMessage()).asRuntimeException());
        }
    }
}
