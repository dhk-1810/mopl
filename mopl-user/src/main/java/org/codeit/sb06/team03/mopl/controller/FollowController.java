package org.codeit.sb06.team03.mopl.controller;

import org.codeit.sb06.team03.mopl.dto.response.FollowDto;
import org.codeit.sb06.team03.mopl.dto.request.FollowRequest;
import org.codeit.sb06.team03.mopl.service.FollowCommandService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/follows")
public class FollowController implements FollowApi {

    private final FollowCommandService followCommandService;

    public FollowController(FollowCommandService followCommandService) {
        this.followCommandService = followCommandService;
    }

    @Override
    @PostMapping
    public ResponseEntity<FollowDto> postFollows(@RequestBody FollowRequest request, @RequestHeader(value = "X-User-Id") UUID userId) {
        UUID followeeId = UUID.fromString(request.followeeId());
        FollowDto response = followCommandService.follow(followeeId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping("/followed-by-me")
    public ResponseEntity<Boolean> getFollowsFollowedByMe(@RequestParam String followeeId, @RequestHeader(value = "X-User-Id") UUID userId) {
        UUID followeeUUID = UUID.fromString(followeeId);
        boolean response = followCommandService.followedByMe(followeeUUID, userId);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/count")
    public ResponseEntity<Long> getFollowersCount(@RequestParam String followeeId) {
        long response = followCommandService.count(followeeId);
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{followId}")
    public ResponseEntity<Void> deleteFollows(@PathVariable String followId, @RequestHeader(value = "X-User-Id") UUID userId) {
        UUID unfollowId = UUID.fromString(followId);
        followCommandService.unfollow(userId, unfollowId);
        return ResponseEntity.noContent().build();
    }
}
