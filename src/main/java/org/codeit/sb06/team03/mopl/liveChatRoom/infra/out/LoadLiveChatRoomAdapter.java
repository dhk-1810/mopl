package org.codeit.sb06.team03.mopl.liveChatRoom.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.liveChatRoom.application.out.LoadLiveChatRoomPort;
import org.codeit.sb06.team03.mopl.liveChatRoom.domain.LiveChatRoom;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoadLiveChatRoomAdapter implements LoadLiveChatRoomPort {

    private final LiveChatRoomRepository liveChatRoomRepository;

    @Override
    public Optional<LiveChatRoom> findById(UUID liveChatRoomId) {
        return liveChatRoomRepository.findById(liveChatRoomId);
    }

    @Override
    public boolean existsById(UUID liveChatRoomId) {
        return liveChatRoomRepository.existsById(liveChatRoomId);
    }
}
