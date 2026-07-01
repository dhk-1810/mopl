package org.codeit.sb06.team03.mopl.liveChatRoom.infra.out;

import org.codeit.sb06.team03.mopl.liveChatRoom.domain.LiveChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LiveChatRoomRepository extends JpaRepository<LiveChatRoom, UUID> {

}
