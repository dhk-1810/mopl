package org.codeit.sb06.team03.mopl.dm.dmMessage.application.in;

import org.codeit.sb06.team03.mopl.dm.dmMessage.domain.DMMessage;

public interface SendDMUseCase {
    DMMessage send(MessageSendCommand command);
}
