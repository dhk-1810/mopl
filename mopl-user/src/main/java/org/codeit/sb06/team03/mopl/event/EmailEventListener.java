package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.event.AccountEvent.PasswordResetedEvent;
import org.codeit.sb06.team03.mopl.email.service.SendEmailUseCase;
import java.time.Instant;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class EmailEventListener {

    private final SendEmailUseCase sendEmailUseCase;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleResetPasswordEvent(PasswordResetedEvent event) {
        final String to = event.getEmailAddress();
        final String rawTempPassword = event.getRawTempPassword();
        final Instant expiresAt = Instant.parse(event.getExpiresAt());
        sendEmailUseCase.sendEmail(to, rawTempPassword, expiresAt);
    }
}
