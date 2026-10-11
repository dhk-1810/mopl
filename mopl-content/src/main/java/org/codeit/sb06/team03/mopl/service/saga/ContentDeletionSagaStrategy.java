package org.codeit.sb06.team03.mopl.service.saga;

import java.util.UUID;

public interface ContentDeletionSagaStrategy {
    void deleteContent(UUID contentId);
}
