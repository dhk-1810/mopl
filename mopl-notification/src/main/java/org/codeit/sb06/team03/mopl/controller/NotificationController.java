package org.codeit.sb06.team03.mopl.controller;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dto.request.CursorRequestNotificationDto;
import org.codeit.sb06.team03.mopl.dto.response.CursorResponseNotificationDto;
import org.codeit.sb06.team03.mopl.service.composite.NotificationCompositeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications")
public class NotificationController implements NotificationApi {

    private final NotificationCompositeService notificationCompositeService;

    @Override
    @GetMapping
    public ResponseEntity<CursorResponseNotificationDto> getNotifications(
            @ModelAttribute CursorRequestNotificationDto request,
            @RequestHeader(value = "X-User-Id") UUID userId
    ) {
        return ResponseEntity.ok(notificationCompositeService.getNotifications(request, userId));
    }

    @Override
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable UUID notificationId,
            @RequestHeader(value = "X-User-Id") UUID userId
    ) {
        notificationCompositeService.deleteNotification(notificationId, userId);
        return ResponseEntity.noContent().build();
    }
}
