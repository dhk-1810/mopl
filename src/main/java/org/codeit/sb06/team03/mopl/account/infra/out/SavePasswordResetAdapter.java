package org.codeit.sb06.team03.mopl.account.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.application.out.SavePasswordResetPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class SavePasswordResetAdapter implements SavePasswordResetPort {

    private final PasswordResetRepository repository;

    @Override
    public void deleteByAccountId(UUID accountId) {
        repository.deleteById(accountId);
    }
}
