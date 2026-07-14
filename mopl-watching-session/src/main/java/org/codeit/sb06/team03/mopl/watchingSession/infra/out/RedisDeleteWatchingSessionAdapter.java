package org.codeit.sb06.team03.mopl.watchingSession.infra.out;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.watchingSession.application.out.DeleteWatchingSessionPort;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Profile("redis")
@Component
@RequiredArgsConstructor
public class RedisDeleteWatchingSessionAdapter implements DeleteWatchingSessionPort {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String WATCHER_KEY_PREFIX = "watching-session:watcher:";
    private static final String LIVE_CHATROOM_KEY_PREFIX = "watching-session:room:";
    private static final String ID_KEY_PREFIX = "watching-session:id:";

    @Override
    public void deleteByWatcherId(UUID watcherId) {
        String watcherKey = WATCHER_KEY_PREFIX + watcherId;
        String json = redisTemplate.opsForValue().get(watcherKey);
        if (json != null) {
            try {
                RedisWatchingSessionDto dto = objectMapper.readValue(json, RedisWatchingSessionDto.class);
                String idKey = ID_KEY_PREFIX + dto.id();
                String roomKey = LIVE_CHATROOM_KEY_PREFIX + dto.liveChatRoomId();

                redisTemplate.delete(watcherKey);
                redisTemplate.delete(idKey);
                redisTemplate.opsForZSet().remove(roomKey, watcherId.toString());
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete watching session from Redis by watcherId", e);
            }
        }
    }

    @Override
    public void deleteById(UUID id) {
        String idKey = ID_KEY_PREFIX + id;
        String watcherIdStr = redisTemplate.opsForValue().get(idKey); // [id : watcherId] 쌍에서 가져옴.
        if (watcherIdStr != null) {
            UUID watcherId = UUID.fromString(watcherIdStr);
            deleteByWatcherId(watcherId); // 한 watcher는 한 WatchingSession만 가지므로 호출.
        }
    }
}
