package org.codeit.sb06.team03.mopl.repository;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.entity.DMMessage;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class DMMessageCustomRepositoryImpl implements DMMessageCustomRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<DMMessage> findAll(
            UUID dmChatRoomId,
            @Nullable String cursor,
            @Nullable String idAfter,
            int limit,
            boolean ascending,
            String sortBy
    ) {
        Criteria criteria = Criteria.where("dmChatRoomId").is(dmChatRoomId)
                .and("isDeleted").is(false);

        if (cursor != null) {
            Instant cursorTime = Instant.parse(cursor);
            UUID idAfterUuid = idAfter == null ? null : UUID.fromString(idAfter);

            if (ascending) {
                Criteria timeCond = Criteria.where("createdAt").gt(cursorTime);
                if (idAfterUuid != null) {
                    Criteria sameTimeIdCond = Criteria.where("createdAt").is(cursorTime)
                            .and("id").gt(idAfterUuid);
                    criteria.orOperator(timeCond, sameTimeIdCond);
                } else {
                    criteria.andOperator(timeCond);
                }
            } else {
                Criteria timeCond = Criteria.where("createdAt").lt(cursorTime);
                if (idAfterUuid != null) {
                    Criteria sameTimeIdCond = Criteria.where("createdAt").is(cursorTime)
                            .and("id").lt(idAfterUuid);
                    criteria.orOperator(timeCond, sameTimeIdCond);
                } else {
                    criteria.andOperator(timeCond);
                }
            }
        }

        Sort.Direction direction = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;
        Query query = new Query(criteria)
                .with(Sort.by(direction, "createdAt", "id"))
                .limit(limit);

        return mongoTemplate.find(query, DMMessage.class);
    }
}
