package org.codeit.sb06.team03.mopl.client;

import org.codeit.sb06.team03.mopl.dto.ContentCreateRequest;
import org.codeit.sb06.team03.mopl.dto.ContentDto;
import org.codeit.sb06.team03.mopl.dto.CursorResponseContentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "mopl-content", url = "${mopl.services.content.url:http://localhost:8082}")
public interface ContentClient {

    @GetMapping("/api/contents")
    CursorResponseContentDto getContents(
            @RequestParam(name = "keywordLike", required = false) String keywordLike,
            @RequestParam(name = "typeEqual", required = false) String typeEqual
    );

    @PostMapping("/api/contents/internal")
    ContentDto createInternal(@RequestBody ContentCreateRequest request);
}
