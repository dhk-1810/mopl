package org.codeit.sb06.team03.mopl.dm.livemessage.application.in;

import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummary;

import java.time.Instant;
import java.util.UUID;

public interface MessagePassUseCase {
    void pass(UUID conversationId, UUID messageId, String content, Instant createdAt, UserSummary sender, UserSummary receiver);
}


