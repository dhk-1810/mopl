package org.codeit.sb06.team03.mopl.repository;

import org.codeit.sb06.team03.mopl.entity.DMMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DMMessageRepository extends MongoRepository<DMMessage, UUID>, DMMessageCustomRepository {

    Optional<DMMessage> findFirstByDmChatRoomIdAndIsDeletedFalseOrderByCreatedAtDescIdDesc(UUID dmChatRoomId);

    long countByDmChatRoomIdAndIsDeletedFalse(UUID dmChatRoomId);
}
