package org.codeit.sb06.team03.mopl.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.entity.PasswordReset;
import org.codeit.sb06.team03.mopl.entity.policy.PasswordEncryptionPolicy;
import org.codeit.sb06.team03.mopl.entity.vo.Password;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PasswordResetService {

    private final PasswordEncryptionPolicy passwordEncryptionPolicy;

    public void validateTempPassword(PasswordReset passwordReset, String tempPassword) {
        Password encryptedInput = passwordEncryptionPolicy.apply(tempPassword);

        if (!passwordReset.validateTempPassword(encryptedInput)) {
            throw new RuntimeException();
        }
    }

}
