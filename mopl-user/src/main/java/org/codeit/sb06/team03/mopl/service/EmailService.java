package org.codeit.sb06.team03.mopl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendEmail(String emailAddress, String rawTempPassword, Instant expireDate) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailAddress);
        message.setSubject("임시 비밀번호 발급");
        message.setText(rawTempPassword + "\n" + expireDate.toString());

        javaMailSender.send(message);
        log.info("Successfully sent temporary password email to: {}", emailAddress);
    }
}
