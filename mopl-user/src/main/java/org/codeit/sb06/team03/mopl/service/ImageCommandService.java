package org.codeit.sb06.team03.mopl.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.event.ImageUploadEvent;
import org.codeit.sb06.team03.mopl.s3.S3Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ImageCommandService {

    private final S3Service s3Service;
    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE_NAME = "mopl.image.exchange";
    private static final String ROUTING_KEY = "mopl.image.upload";

    public String register(UUID userId, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return null;
        }
        try {
            // 1. 이미지 S3 Key 생성 (profiles/{userId}/{UUID}.{확장자})
            String originalFilename = image.getOriginalFilename();
            String extension = (originalFilename != null && !originalFilename.isBlank())
                    ? StringUtils.getFilenameExtension(originalFilename)
                    : null;
            String fileIdentifier = (extension != null && !extension.isBlank())
                    ? UUID.randomUUID() + "." + extension.toLowerCase()
                    : UUID.randomUUID().toString();
            String key = "profiles/" + userId + "/" + fileIdentifier;

            // 2. 유저 서비스에서 직접 S3 업로드 수행 (통신 오버헤드 감소)
            s3Service.uploadFile(key, image);

            // 3. 이미지 서비스 측에 캐시 생성(TimeoutImage)만 래빗MQ로 요청
            ImageUploadEvent event = new ImageUploadEvent(
                    key,
                    image.getContentType()
            );
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, event);

            return key;
        } catch (IOException e) {
            throw new RuntimeException("S3 direct upload failed in user-service", e);
        }
    }
}
