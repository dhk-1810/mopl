package org.codeit.sb06.team03.mopl.repository.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.entity.WatchingSession;
import org.codeit.sb06.team03.mopl.dto.WatchingSessionReadModel;
import org.codeit.sb06.team03.mopl.repository.WatchingSessionRepository;
import org.codeit.sb06.team03.mopl.repository.postgres.WatchingSessionSearchCondition;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Profile("redis")
@Repository
@RequiredArgsConstructor
public class WatchingSessionRedisRepository implements WatchingSessionRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String WATCHER_KEY_PREFIX = "watching-session:watcher:";
    private static final String LIVE_CHATROOM_KEY_PREFIX = "watching-session:room:";
    private static final String ID_KEY_PREFIX = "watching-session:id:";

    /**
     * 저장
     */
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

    /**
     * 조회
     */
    @Override
    public boolean existsByLiveChatRoomIdAndWatcherId(UUID liveChatRoomId, UUID watcherId) {
        String roomKey = LIVE_CHATROOM_KEY_PREFIX + liveChatRoomId;
        Double score = redisTemplate.opsForZSet().score(roomKey, watcherId.toString());  // Sorted Set은 [값(Member), 점수(Score)] 쌍을 저장.
        return score != null;
    }

    @Override
    public long countByContentId(UUID contentId) {
        String roomKey = LIVE_CHATROOM_KEY_PREFIX + contentId;
        Long count = redisTemplate.opsForZSet().zCard(roomKey); // zCard(): ZSet의 원소(멤버) 개수 반환
        return count != null ? count : 0L;
    }

    @Override
    public Optional<WatchingSessionReadModel> findReadModelByWatcherId(UUID watcherId) {
        String watcherKey = WATCHER_KEY_PREFIX + watcherId;
        String json = redisTemplate.opsForValue().get(watcherKey);
        if (json == null) {
            return Optional.empty();
        }
        try {
            RedisWatchingSessionDto dto = objectMapper.readValue(json, RedisWatchingSessionDto.class);
            return Optional.of(new WatchingSessionReadModel(
                    UUID.fromString(dto.id()),
                    UUID.fromString(dto.watcherId()),
                    UUID.fromString(dto.liveChatRoomId()),
                    Instant.ofEpochMilli(dto.createdAtEpochMilli())
            ));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<WatchingSessionReadModel> findReadModelByLiveChatRoomIdAndWatcherId(UUID liveChatRoomId, UUID watcherId) {
        return findReadModelByWatcherId(watcherId)
                .filter(readModel -> readModel.liveChatRoomId().equals(liveChatRoomId));
    }

    @Override
    public Slice<WatchingSessionReadModel> findReadModelByContentId(WatchingSessionSearchCondition condition) {
        String roomKey = LIVE_CHATROOM_KEY_PREFIX + condition.contentId();
        int limit = condition.limit();
        String direction = condition.sortDirection() != null ? condition.sortDirection() : "ASCENDING";

        // 정렬 방향에 맞춰 시간(Score) 조회 범위(minScore, maxScore) 설정
        double minScore = Double.NEGATIVE_INFINITY;
        double maxScore = Double.POSITIVE_INFINITY;

        if (condition.cursor() != null) {
            long cursorMilli = condition.cursor().toEpochMilli();
            if ("ASCENDING".equalsIgnoreCase(direction)) {
                minScore = cursorMilli; // ASC일 경우: 커서 시각 이후 데이터만
            } else {
                maxScore = cursorMilli; // DESC일 경우: 커서 시각 이전 데이터만
            }
        }

        // ZSet에서 시간 조건에 부합하는 watcherId 목록 조회
        Set<String> watcherIdStrings;
        if ("ASCENDING".equalsIgnoreCase(direction)) {
            watcherIdStrings = redisTemplate.opsForZSet().rangeByScore(roomKey, minScore, maxScore, 0, limit + 1);
        } else {
            watcherIdStrings = redisTemplate.opsForZSet().reverseRangeByScore(roomKey, minScore, maxScore, 0, limit + 1);
        }

        if (watcherIdStrings == null || watcherIdStrings.isEmpty()) {
            return new SliceImpl<>(List.of(), PageRequest.ofSize(limit), false);
        }

        List<String> keys = watcherIdStrings.stream()
                .map(id -> WATCHER_KEY_PREFIX + id)
                .toList();
        List<String> jsons = redisTemplate.opsForValue().multiGet(keys); // watcherId별 데이터를 한 번에 조회

        // JSON 데이터를 역직렬화, 특정 시청자 ID 필터링 조건이 있을 경우 적용
        List<WatchingSessionReadModel> list = new ArrayList<>();
        if (jsons != null) {
            for (String json : jsons) {
                if (json != null) {
                    try {
                        RedisWatchingSessionDto dto = objectMapper.readValue(json, RedisWatchingSessionDto.class);
                        UUID watcherUuid = UUID.fromString(dto.watcherId());
                        if (condition.watcherIds() != null && !condition.watcherIds().isEmpty() && !condition.watcherIds().contains(watcherUuid)) {
                            continue;
                        }
                        list.add(new WatchingSessionReadModel(
                                UUID.fromString(dto.id()),
                                watcherUuid,
                                UUID.fromString(dto.liveChatRoomId()),
                                Instant.ofEpochMilli(dto.createdAtEpochMilli())
                        ));
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }

        // tie-break
        if (condition.cursor() != null && condition.idAfter() != null) {
            UUID idAfter = condition.idAfter();
            Instant cursor = condition.cursor();
            list = list.stream().filter(item -> {
                int timeCompare = item.createdAt().compareTo(cursor);
                if ("ASCENDING".equalsIgnoreCase(direction)) {
                    if (timeCompare > 0) return true;
                    if (timeCompare == 0) {
                        return item.id().compareTo(idAfter) > 0;
                    }
                    return false;
                } else {
                    if (timeCompare < 0) return true;
                    if (timeCompare == 0) {
                        return item.id().compareTo(idAfter) < 0;
                    }
                    return false;
                }
            }).collect(Collectors.toList());
        }

        boolean hasNext = false;
        if (list.size() > limit) {
            list = list.subList(0, limit);
            hasNext = true;
        }

        return new SliceImpl<>(list, PageRequest.ofSize(limit), hasNext);
    }

    // 객체 형태로 Application 계층에 보내줘야 할때 사용.
    private WatchingSession reconstruct(RedisWatchingSessionDto dto) {
        try {
            java.lang.reflect.Constructor<WatchingSession> constructor = WatchingSession.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            WatchingSession session = constructor.newInstance();

            setField(session, "id", UUID.fromString(dto.id()));
            setField(session, "watcherId", UUID.fromString(dto.watcherId()));
            setField(session, "liveChatRoomId", UUID.fromString(dto.liveChatRoomId()));
            setField(session, "createdAt", Instant.ofEpochMilli(dto.createdAtEpochMilli()));

            return session;
        } catch (Exception e) {
            throw new RuntimeException("Failed to reconstruct WatchingSession", e);
        }
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = WatchingSession.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * 삭제
     */
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
