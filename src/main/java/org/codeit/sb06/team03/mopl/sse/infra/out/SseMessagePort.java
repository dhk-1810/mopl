package org.codeit.sb06.team03.mopl.sse.infra.out;

import org.codeit.sb06.team03.mopl.sse.SseMessage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SseMessagePort {

    void saveMessage(SseMessage message, UUID userId);

    void saveMessages(Map<UUID, SseMessage> messages);

    List<SseMessage> findAllMissedMessageByUserIdAndIdAfter(UUID userId, UUID lastMessageId);

    List<SseMessage> findAllMissedMessageByUserIdAndIdAfter(UUID userId, UUID lastMessageId);

    void deleteAll(UUID userId);
}
