package org.codeit.sb06.team03.mopl.repository;

import org.codeit.sb06.team03.mopl.entity.LiveChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LiveChatRoomRepository extends JpaRepository<LiveChatRoom, UUID> {

}
