package org.codeit.sb06.team03.mopl.client;

import org.codeit.sb06.team03.mopl.dto.ContentCreateRequest;
import org.codeit.sb06.team03.mopl.dto.ContentDto;
import org.codeit.sb06.team03.mopl.dto.CursorResponseContentDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ContentClient {

    private final RestClient restClient;

    public ContentClient(@Value("${mopl.services.content.url:http://localhost:8082}") String contentUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(contentUrl)
                .build();
    }

    public CursorResponseContentDto getContents(String keywordLike, String typeEqual) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/contents")
                        .queryParam("keywordLike", keywordLike)
                        .queryParam("typeEqual", typeEqual)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(CursorResponseContentDto.class);
    }

    public ContentDto createInternal(ContentCreateRequest request) {
        return restClient.post()
                .uri("/api/contents/internal")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ContentDto.class);
    }
}

