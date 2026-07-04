package org.codeit.sb06.team03.mopl.account.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.application.in.GetAccountUseCase;
import org.codeit.sb06.team03.mopl.account.application.out.LoadAccountPort;
import org.codeit.sb06.team03.mopl.account.domain.Account;
import org.codeit.sb06.team03.mopl.account.domain.exception.AccountNotFoundException;
import org.codeit.sb06.team03.mopl.account.domain.vo.EmailAddress;
import org.codeit.sb06.team03.mopl.profile.infra.in.CursorRequestUserDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class AccountQueryService implements GetAccountUseCase {

    private final LoadAccountPort loadAccountPort;

    @Override
    public Slice<Account> getById(CursorRequestUserDto request) {
        final List<Account> accounts = loadAccountPort.findAll(request);
        final Integer limit = request.limit();
        
        final List<Account> data = accounts.size() > limit ? accounts.subList(0, limit) : accounts;
        final Boolean hasNext = accounts.size() > limit;
        
        Pageable pageable = PageRequest.of(0, limit);
        return new SliceImpl<>(data, pageable, hasNext);
    }

    @Override
    public Account getById(UUID accountId) {
        return loadAccountPort.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    @Override
    public Optional<Account> getByEmail(String email) {
        return loadAccountPort.findByEmailAddress(new EmailAddress(email));
    }

    @Override
    public Long count(CursorRequestUserDto request) {
        return loadAccountPort.count(request);
    }
}
