package org.codeit.sb06.team03.mopl.email.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.email.service.SendEmailCommand;
import org.codeit.sb06.team03.mopl.email.service.SendEmailUseCase;
import org.codeit.sb06.team03.mopl.email.domain.Email;
import org.codeit.sb06.team03.mopl.email.domain.EmailService;
import org.codeit.sb06.team03.mopl.email.domain.event.EmailEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@RequiredArgsConstructor
@Service
@Transactional(value = "notificationTransactionManager", readOnly = true)
public class EmailAppService implements SendEmailUseCase {

    private final ApplicationEventPublisher eventPublisher;
    private final EmailService emailService;

    @Override
    @Transactional("notificationTransactionManager")
    public void sendEmail(SendEmailCommand command) {
        final String emailAddress = command.emailAddress();
        final String rawTempPassword = command.rawTempPassword();
        final Instant expireDate = command.expireDate();

        Email email = emailService.send(emailAddress, rawTempPassword, expireDate);

//        eventPublisher.publishEvent(new EmailEvent.EmailSentEvent());
        // TODO : 이벤트
    }
}