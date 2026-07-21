package org.codeit.sb06.team03.mopl.controller;
import org.codeit.sb06.team03.mopl.dto.response.CursorResponseContentDto;
import org.codeit.sb06.team03.mopl.dto.request.ContentCreateRequest;
import org.codeit.sb06.team03.mopl.dto.request.ContentUpdateRequest;

import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.service.composite.ContentCompositeService;
import org.codeit.sb06.team03.mopl.dto.response.ContentDto;
import org.codeit.sb06.team03.mopl.dto.request.CursorRequestContentDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import org.codeit.sb06.team03.mopl.dto.request.ContentCreateInternalRequest;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentCompositeService contentCompositeService;

    @GetMapping
    public ResponseEntity<CursorResponseContentDto> getContents(@ModelAttribute CursorRequestContentDto request){
        return ResponseEntity.ok(contentCompositeService.getContents(request));
    }

    @GetMapping("/{contentId}")
    public ResponseEntity<ContentDto> getSingleContent(@PathVariable UUID contentId){
        return ResponseEntity.ok(contentCompositeService.getSingleContent(contentId));
    }

    @PostMapping("/internal")
    public ResponseEntity<ContentDto> createInternal(
            @RequestBody ContentCreateInternalRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contentCompositeService.createInternal(request));
    }

    @PostMapping
    @RolesAllowed("ADMIN")
    public ResponseEntity<ContentDto> create(
            @RequestBody ContentCreateRequest request,
            @RequestPart(name = "thumbnail") MultipartFile thumbnail
    ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contentCompositeService.create(request, thumbnail));
    }

    @PatchMapping("/{contentId}")
    @RolesAllowed("ADMIN")
    public ResponseEntity<ContentDto> update(
            @PathVariable UUID contentId,
            @RequestBody ContentUpdateRequest request
    ) {
        return ResponseEntity.ok(contentCompositeService.update(contentId, request));
    }

    @DeleteMapping("/{contentId}")
    @RolesAllowed("ADMIN")
    public ResponseEntity<Void> delete(@PathVariable UUID contentId){
        contentCompositeService.delete(contentId);
        return ResponseEntity.noContent().build();
    }
}
