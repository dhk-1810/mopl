package org.codeit.sb06.team03.mopl.sse.repository;

import org.codeit.sb06.team03.mopl.sse.service.SseMessage;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * In-Memory Repository
 */
@Repository
public class SseRepository {

    // SseEmitter 저장소
    private final ConcurrentMap<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>(); // List로 다중 연결 허용

    // SseMessage 저장소 (이벤트 유실 복원용)
    private final ConcurrentMap<UUID, List<SseMessage>> messages = new ConcurrentHashMap<>();

    private static final int MAX_MESSAGE_HISTORY = 10;

    /**
     * SseEmitter
     */

    public void saveEmitter(SseEmitter emitter, UUID userId) {
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
    }

    public Set<UUID> findAllConnectedUserIds() {
        return emitters.keySet();
    }

    public List<SseEmitter> findEmittersByUserId(UUID userId) {
        return emitters.getOrDefault(userId, Collections.emptyList());
    }

    public Map<UUID, List<SseEmitter>> findAllEmittersByUserIdIn(Set<UUID> userIds) {
        return userIds.stream()
                .filter(emitters::containsKey)
                .collect(Collectors.toMap(
                        userId -> userId,
                        userId -> new ArrayList<>(emitters.get(userId))
                ));
    }

    // SSE 연결 종료 시 사용
    public void deleteEmitter(SseEmitter emitter, UUID userId) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId); // 리스트가 비어있으면 Map에서 삭제
            }
        }
    }

    // 사용자의 모든 SseEmitter 삭제 (로그아웃)
    public void deleteAllEmittersByUserId(UUID userId) {
        emitters.remove(userId);
    }


    /**
     * SseMessage
     */

    public void saveMessage(SseMessage message, UUID userId) {
        messages.compute(userId, (id, history) -> {
            List<SseMessage> list = (history == null) ? new CopyOnWriteArrayList<>() : history;
            list.add(message);

            while (list.size() > MAX_MESSAGE_HISTORY) {
                list.removeFirst();
            }
            return list;
        });
    }

    public void saveAllMessages(Map<UUID, SseMessage> messagesToSave) {
        messagesToSave.forEach((userId, message) -> saveMessage(message, userId));
    }

    public List<SseMessage> findAllMissedMessageByUserIdAndIdAfter(UUID userId, UUID lastMessageId) {

        List<SseMessage> history = messages.getOrDefault(userId, Collections.emptyList());

        // lastMessageId가 위치한 인덱스 찾기
        int lastSeenIndex = -1;
        for (int i = 0; i < history.size(); i++) {
            if (history.get(i).id().equals(lastMessageId)) {
                lastSeenIndex = i;
                break;
            }
        }
        return new ArrayList<>(history.subList(lastSeenIndex + 1, history.size()));
    }

    // 사용자의 모든 SseMessage 삭제 (로그아웃)
    public void deleteAllMessagesByUserId(UUID userId) {
        messages.remove(userId);
    }
}
