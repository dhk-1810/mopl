package org.codeit.sb06.team03.mopl.event;

import java.util.UUID;

public record ContentDeletionSagaEvent(
        UUID sagaId,
        UUID contentId,
        String participant, // "PLAYLIST", "WATCHING_SESSION"
        String status,      // "STARTED", "SUCCESS", "FAILED"
        String reason
) {
    public static ContentDeletionSagaEvent start(UUID sagaId, UUID contentId) {
        return new ContentDeletionSagaEvent(sagaId, contentId, "SAGA_INIT", "STARTED", null);
    }

    public static ContentDeletionSagaEvent success(UUID sagaId, UUID contentId, String participant) {
        return new ContentDeletionSagaEvent(sagaId, contentId, participant, "SUCCESS", null);
    }

    public static ContentDeletionSagaEvent failed(UUID sagaId, UUID contentId, String participant, String reason) {
        return new ContentDeletionSagaEvent(sagaId, contentId, participant, "FAILED", reason);
    }
}
