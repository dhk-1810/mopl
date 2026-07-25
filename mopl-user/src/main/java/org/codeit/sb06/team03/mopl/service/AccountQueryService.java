package org.codeit.sb06.team03.mopl.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.entity.Account;
import org.codeit.sb06.team03.mopl.exception.account.AccountNotFoundException;
import org.codeit.sb06.team03.mopl.entity.vo.EmailAddress;
import org.codeit.sb06.team03.mopl.dto.request.CursorRequestUserDto;
import org.codeit.sb06.team03.mopl.repository.AccountRepository;
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
public class AccountQueryService {

    private final AccountRepository accountRepository;

    public Slice<Account> getById(CursorRequestUserDto request) {
        final List<Account> accounts = accountRepository.findAllAccounts(request);
        final Integer limit = request.limit();
        
        final List<Account> data = accounts.size() > limit ? accounts.subList(0, limit) : accounts;
        final Boolean hasNext = accounts.size() > limit;
        
        Pageable pageable = PageRequest.of(0, limit);
        return new SliceImpl<>(data, pageable, hasNext);
    }

    public Account getById(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    public Optional<Account> getByEmail(String email) {
        return accountRepository.findByEmailAddress(new EmailAddress(email));
    }

    public Long count(CursorRequestUserDto request) {
        return accountRepository.count(request);
    }
}
