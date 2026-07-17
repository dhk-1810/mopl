package org.codeit.sb06.team03.mopl.email.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.domain.event.AccountEvent.PasswordResetedEvent;
import org.codeit.sb06.team03.mopl.email.controller.PasswordResetMapper;
import org.codeit.sb06.team03.mopl.email.service.SendEmailCommand;
import org.codeit.sb06.team03.mopl.email.service.SendEmailUseCase;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class EmailEventListener {

    private final SendEmailUseCase sendEmailUseCase;
    private final PasswordResetMapper passwordResetMapper;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleResetPasswordEvent(PasswordResetedEvent event) {
        SendEmailCommand command = passwordResetMapper.toCommand(event);
        sendEmailUseCase.sendEmail(command);
    }
}
