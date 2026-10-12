package org.codeit.sb06.team03.mopl.event;

import java.util.UUID;

public record ContentDeletionSagaEvent(
        UUID sagaId,
        UUID contentId,
        String participant, // "PLAYLIST", "WATCHING_SESSION", "SAGA_INIT"
        String status,      // "STARTED", "SUCCESS", "FAILED"
        String reason,
        String action       // "START", "COMPENSATE"
) {
    public ContentDeletionSagaEvent {
        if (action == null) {
            action = "START";
        }
    }

    public ContentDeletionSagaEvent(UUID sagaId, UUID contentId, String participant, String status, String reason) {
        this(sagaId, contentId, participant, status, reason, "START");
    }

    public static ContentDeletionSagaEvent start(UUID sagaId, UUID contentId) {
        return new ContentDeletionSagaEvent(sagaId, contentId, "SAGA_INIT", "STARTED", null, "START");
    }

    public static ContentDeletionSagaEvent success(UUID sagaId, UUID contentId, String participant) {
        return new ContentDeletionSagaEvent(sagaId, contentId, participant, "SUCCESS", null, "START");
    }

    public static ContentDeletionSagaEvent failed(UUID sagaId, UUID contentId, String participant, String reason) {
        return new ContentDeletionSagaEvent(sagaId, contentId, participant, "FAILED", reason, "START");
    }

    public static ContentDeletionSagaEvent compensate(UUID sagaId, UUID contentId) {
        return new ContentDeletionSagaEvent(sagaId, contentId, "SAGA_INIT", "FAILED", "Compensating transaction", "COMPENSATE");
    }

    public static ContentDeletionSagaEvent compensate(UUID sagaId, UUID contentId, String targetParticipant) {
        return new ContentDeletionSagaEvent(sagaId, contentId, targetParticipant, "FAILED", "Compensating transaction", "COMPENSATE");
    }
}
