package org.codeit.sb06.team03.mopl.liveChatRoom.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.liveChatRoom.application.out.SaveLiveChatRoomPort;
import org.codeit.sb06.team03.mopl.liveChatRoom.domain.LiveChatRoom;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveLiveChatRoomAdapter implements SaveLiveChatRoomPort {

    private final LiveChatRoomRepository liveChatRoomRepository;

    @Override
    public LiveChatRoom save(LiveChatRoom liveChatRoom) {
        return liveChatRoomRepository.save(liveChatRoom);
    }
}
