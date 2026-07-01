package org.codeit.sb06.team03.mopl.notification.infra.in;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.composite.NotificationCompositeService;
import org.codeit.sb06.team03.mopl.security.MoplUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications")
public class NotificationController implements NotificationApi {

    private final NotificationCompositeService notificationCompositeService;

    @Override
    @GetMapping
    public ResponseEntity<CursorResponseNotificationDto> getNotifications(
            @ModelAttribute CursorRequestNotificationDto request,
            @AuthenticationPrincipal MoplUserDetails user
    ) {
        return ResponseEntity.ok(notificationCompositeService.getNotifications(request, user.getId()));
    }

    @Override
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable String notificationId,
            @AuthenticationPrincipal MoplUserDetails user
    ) {
        notificationCompositeService.deleteNotification(notificationId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
