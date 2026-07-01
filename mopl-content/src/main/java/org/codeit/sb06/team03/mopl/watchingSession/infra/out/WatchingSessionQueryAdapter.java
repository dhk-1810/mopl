package org.codeit.sb06.team03.mopl.watchingSession.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.liveChatRoom.application.out.LiveChatRoomWatchingSessionQueryPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WatchingSessionQueryAdapter implements LiveChatRoomWatchingSessionQueryPort {

    private final WatchingSessionRepository repository;

    @Override
    public long countByLiveChatRoomId(UUID liveChatRoomId) {
        return repository.countByLiveChatRoomId(liveChatRoomId);
    }
}
