package org.codeit.sb06.team03.mopl.dm.livemessage.application.out;

import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummaryDto;

import java.util.UUID;

public interface LoadLiveDMUserPort {
    UserSummaryDto findByUserId(UUID userId);
}


