package org.codeit.sb06.team03.mopl.dm.dmMessage.application.out;

import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummary;

import java.time.Instant;
import java.util.UUID;

public interface MessagePassPort {

    void pass(UUID dmChatRoomId, UUID messageId, String content, Instant createdAt, UserSummary sender, UserSummary receiver);
}