package org.codeit.sb06.team03.mopl.dm.dmMessage.application.out;

import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummary;

import java.util.UUID;

public interface LoadLiveDMUserPort {
    UserSummary findByUserId(UUID userId);
}


