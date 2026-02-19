package org.codeit.sb06.team03.mopl.notification.infra.in;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.security.MoplUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications")
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    @Override
    @GetMapping
    public ResponseEntity<CursorResponseNotificationDto> getNotifications(
            @ModelAttribute CursorRequestNotificationDto request,
            @AuthenticationPrincipal MoplUserDetails user
    ) {
        return ResponseEntity.ok(notificationService.getAll());
    }

    @Override
    @PostMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable String notificationId,
            @AuthenticationPrincipal MoplUserDetails user
    ) {
        notificationService.deleteById();
        return ResponseEntity.noContent().build();
    }
}
