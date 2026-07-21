package org.codeit.sb06.team03.mopl.client;

import org.codeit.sb06.team03.mopl.dto.ContentCreateRequest;
import org.codeit.sb06.team03.mopl.dto.ContentDto;
import org.codeit.sb06.team03.mopl.dto.CursorResponseContentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "mopl-content", url = "${mopl.services.content.url:http://localhost:8080}")
public interface ContentClient {

    @GetMapping("/api/contents")
    CursorResponseContentDto getContents(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String type
    );

    @PostMapping("/api/contents/internal")
    ContentDto createInternal(@RequestBody ContentCreateRequest request);
}
