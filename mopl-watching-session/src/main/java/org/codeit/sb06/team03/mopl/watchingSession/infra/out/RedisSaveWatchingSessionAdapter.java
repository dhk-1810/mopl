package org.codeit.sb06.team03.mopl.watchingSession.infra.out;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.watchingSession.application.out.SaveWatchingSessionPort;
import org.codeit.sb06.team03.mopl.watchingSession.domain.WatchingSession;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Profile("redis")
@Component
@RequiredArgsConstructor
public class RedisSaveWatchingSessionAdapter implements SaveWatchingSessionPort {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String WATCHER_KEY_PREFIX = "watching-session:watcher:";
    private static final String LIVE_CHATROOM_KEY_PREFIX = "watching-session:room:";
    private static final String ID_KEY_PREFIX = "watching-session:id:";

    @Override
    public WatchingSession save(WatchingSession watchingSession) {
        String watcherKey = WATCHER_KEY_PREFIX + watchingSession.getWatcherId();
        String roomKey = LIVE_CHATROOM_KEY_PREFIX + watchingSession.getLiveChatRoomId();
        String idKey = ID_KEY_PREFIX + watchingSession.getId();

        try {
            RedisWatchingSessionDto dto = RedisWatchingSessionDto.from(watchingSession);
            String json = objectMapper.writeValueAsString(dto);

            // 단건 WatchingSession 정보 저장
            redisTemplate.opsForValue().set(watcherKey, json);

            // id-to-watcherId 매핑 저장
            redisTemplate.opsForValue().set(idKey, watchingSession.getWatcherId().toString());

            // 채팅방 참여자 리스트에 추가 (ZSet)
            redisTemplate.opsForZSet().add(roomKey, watchingSession.getWatcherId().toString(), watchingSession.getCreatedAt().toEpochMilli());

            return watchingSession;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save watching session to Redis", e);
        }
    }
}
