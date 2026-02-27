package org.codeit.sb06.team03.mopl.sse.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.sse.SseMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class SseMessageAdapter implements SseMessagePort{

    private final SseRepository repository;

    @Override
    public void saveMessage(SseMessage message, UUID userId) {
        repository.saveMessage(message, userId);
    }

    @Override
    public void saveMessages(Map<UUID, SseMessage> messages) {
        repository.saveAllMessages(messages);
    }

    @Override
    public List<SseMessage> findAllMissedMessageByUserIdAndIdAfter(UUID userId, UUID lastMessageId) {
        return repository.findAllMissedMessageByUserIdAndIdAfter(userId, lastMessageId);

    @Override
    public void deleteAll(UUID userId) {
        repository.deleteAllMessagesByUserId(userId);
    }
}
