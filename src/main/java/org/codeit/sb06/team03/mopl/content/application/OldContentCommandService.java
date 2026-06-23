package org.codeit.sb06.team03.mopl.content.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.WatchingSessionDto;
import org.codeit.sb06.team03.mopl.content.Content;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.application.in.CursorResponseWatchingSessionDto;
import org.codeit.sb06.team03.mopl.content.application.in.GetContentUseCase;
import org.codeit.sb06.team03.mopl.content.application.in.WatchingSessionCursorCommand;
import org.codeit.sb06.team03.mopl.content.application.out.LoadContentPort;
import org.codeit.sb06.team03.mopl.content.application.out.WatchingSessionSearchCondition;
import org.codeit.sb06.team03.mopl.content.domain.exception.ContentNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OldContentCommandService implements GetContentUseCase {

    private final LoadContentPort loadContentPort;

    @Override
    public ContentReadModel get(UUID contentId) {
        Content content = loadContentPort.findByIdWithTags(contentId)
                .orElseThrow(() -> ContentNotFoundException.fromId(contentId));
        return ContentReadModel.from(content);
    }

    @Override
    public CursorResponseWatchingSessionDto get(WatchingSessionCursorCommand command) {
        UUID contentId = UUID.fromString(command.contentId());
        Instant cursor = command.cursor() != null ? Instant.parse(command.cursor()) : null;
        UUID idAfter =  command.idAfter() != null ? UUID.fromString(command.idAfter()) : null;

        WatchingSessionSearchCondition query = new WatchingSessionSearchCondition(
                contentId,
                command.watcherNameLike(),
                cursor,
                idAfter,
                command.limit(),
                command.sortDirection(),
                command.sortBy()
        );

        List<WatchingSessionDto> sessionsDetails = loadContentPort.findSessionsDetails(query);

        boolean hasNext =  sessionsDetails.size() > command.limit();
        if (hasNext) {
            sessionsDetails.remove(command.limit());
        }

        String nextCursor = hasNext ? sessionsDetails.getLast().createdAt().toString() : null;
        String nextIdAfter = hasNext ? sessionsDetails.getLast().id().toString() : null;

        long totalCount = loadContentPort.countByContentIdAndWatcherNameLike(contentId, command.watcherNameLike());

        return new CursorResponseWatchingSessionDto(
                sessionsDetails,
                nextCursor,
                nextIdAfter,
                hasNext,
                totalCount,
                command.sortDirection(),
                command.sortBy()
        );
    }
}
