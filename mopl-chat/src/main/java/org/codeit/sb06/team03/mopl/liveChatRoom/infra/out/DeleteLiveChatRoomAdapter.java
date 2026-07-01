package org.codeit.sb06.team03.mopl.liveChatRoom.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.liveChatRoom.application.out.DeleteLiveChatRoomPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeleteLiveChatRoomAdapter implements DeleteLiveChatRoomPort {

    private final LiveChatRoomRepository liveChatRoomRepository;

    @Override
    public void deleteById(UUID contentId) {
        liveChatRoomRepository.deleteById(contentId);
    }
}
