package org.codeit.sb06.team03.mopl.email.service;

import java.time.Instant;

public interface SendEmailUseCase {
    void sendEmail(String emailAddress, String rawTempPassword, Instant expireDate);
}
