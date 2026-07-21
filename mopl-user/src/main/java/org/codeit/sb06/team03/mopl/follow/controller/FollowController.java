package org.codeit.sb06.team03.mopl.follow.controller;

import org.codeit.sb06.team03.mopl.follow.service.FollowCommandService;
import org.codeit.sb06.team03.mopl.follow.service.*;
import org.codeit.sb06.team03.mopl.follow.controller.FollowMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/follows")
public class FollowController implements FollowApi {

    private final FollowCommandService followCommandService;
    private final FollowMapper mapper;

    public FollowController(
            FollowCommandService followCommandService,
            FollowMapper mapper
    ) {
        this.followCommandService = followCommandService;
        this.mapper = mapper;
    }

    @Override
    @PostMapping
    public ResponseEntity<FollowDto> postFollows(@RequestBody FollowRequest request, @RequestHeader(value = "X-User-Id") UUID userId) {
        FollowCommand command = mapper.toCommand(request, userId);
        FollowDto response = followCommandService.follow(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @GetMapping("/followed-by-me")
    public ResponseEntity<Boolean> getFollowsFollowedByMe(@RequestParam String followeeId, @RequestHeader(value = "X-User-Id") UUID userId) {
        FollowQuery query = mapper.toQuery(followeeId, userId);
        boolean response = followCommandService.followedByMe(query);
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
        UnfollowCommand command = mapper.toCommand(followId, userId);
        followCommandService.unfollow(command);
        return ResponseEntity.noContent().build();
    }
}
