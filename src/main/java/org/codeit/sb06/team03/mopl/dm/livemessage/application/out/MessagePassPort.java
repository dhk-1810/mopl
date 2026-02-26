package org.codeit.sb06.team03.mopl.dm.livemessage.application.out;

import org.codeit.sb06.team03.mopl.dm.conversation.domain.vo.DMUser;

import java.time.Instant;
import java.util.UUID;

public interface MessagePassPort {

    void pass(UUID conversationId, UUID messageId, String content, Instant createdAt, DMUser sender, DMUser receiver);
}