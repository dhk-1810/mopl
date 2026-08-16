package org.codeit.sb06.team03.mopl.repository;

import org.codeit.sb06.team03.mopl.entity.DMMessage;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.UUID;

public interface DMMessageCustomRepository {
    List<DMMessage> findAll(
            UUID dmChatRoomId,
            @Nullable String cursor,
            @Nullable String idAfter,
            int limit,
            boolean ascending,
            String sortBy
    );
}
