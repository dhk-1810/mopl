package org.codeit.sb06.team03.mopl.account.infra.out;

import org.codeit.sb06.team03.mopl.account.application.out.LoadAccountPort;
import org.codeit.sb06.team03.mopl.account.domain.Account;
import org.codeit.sb06.team03.mopl.account.domain.vo.EmailAddress;
import org.codeit.sb06.team03.mopl.profile.infra.in.CursorRequestUserDto;
import org.codeit.sb06.team03.mopl.profile.infra.in.UserDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class LoadAccountAdapter implements LoadAccountPort {

    private final AccountRepository repository;

    public LoadAccountAdapter(AccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByEmailAddress(EmailAddress emailAddress) {
        return repository.existsByEmailAddress(emailAddress);
    }

    @Override
    public Optional<Account> findById(UUID accountId) {
        return repository.findById(accountId);
    }

    @Override
    public Optional<Account> findByEmailAddress(EmailAddress emailAddress) {
        return repository.findByEmailAddress(emailAddress);
    }

    @Override
    public List<Account> findAll(CursorRequestUserDto query) {
        return repository.findAllAccounts(query);
    }

    @Override
    public Long count(CursorRequestUserDto query) {
        return repository.count(query);
    }

    @Override
    public Optional<UserDto> findById(String accountId) {
        return repository.findAccountById(accountId)
                .map(account -> UserDto.from(account, account.getProfile(), null));
    }
}
