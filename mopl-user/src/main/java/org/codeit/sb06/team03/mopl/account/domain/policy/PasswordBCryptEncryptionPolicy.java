package org.codeit.sb06.team03.mopl.account.domain.policy;

import org.codeit.sb06.team03.mopl.account.domain.vo.Password;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "mopl.account.password-policy", havingValue = "bcrypt")
public class PasswordBCryptEncryptionPolicy implements PasswordEncryptionPolicy {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Password apply(String rawPassword) {
        return new Password(passwordEncoder.encode(rawPassword));
    }
}
