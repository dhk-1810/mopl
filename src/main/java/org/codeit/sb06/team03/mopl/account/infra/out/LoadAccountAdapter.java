package org.codeit.sb06.team03.mopl.account.infra.out;

import org.codeit.sb06.team03.mopl.account.application.out.LoadAccountPort;
import org.codeit.sb06.team03.mopl.account.domain.Account;
import org.codeit.sb06.team03.mopl.account.domain.vo.EmailAddress;
import org.codeit.sb06.team03.mopl.image.application.in.GetPresignedUrlUseCase;
import org.codeit.sb06.team03.mopl.profile.infra.in.CursorRequestUserDto;
import org.codeit.sb06.team03.mopl.profile.infra.in.UserDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class LoadAccountAdapter implements LoadAccountPort {

    private final AccountRepository repository;
    private final GetPresignedUrlUseCase getPresignedUrlUseCase;

    public LoadAccountAdapter(AccountRepository repository, GetPresignedUrlUseCase getPresignedUrlUseCase) {
        this.repository = repository;
        this.getPresignedUrlUseCase = getPresignedUrlUseCase;
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
    public List<UserDto> findAll(CursorRequestUserDto query) {
        List<Account> accounts = repository.findAllAccounts(query);
        List<String> imageKeys = accounts.stream()
                .map(acc -> acc.getProfile().getImageKey())
                .toList();
        Map<String, String> urls = getPresignedUrlUseCase.getPresignedUrls(imageKeys);
        return accounts.stream()
                .map(account -> UserDto.from(account, account.getProfile(), urls.get(account.getProfile().getImageKey())))
                .toList();
    }

    @Override
    public Long count(CursorRequestUserDto query) {
        return repository.count(query);
    }

    @Override
    public Optional<UserDto> findById(String accountId) {
        return repository.findAccountById(accountId)
                .map(account -> {
                    String url = getPresignedUrlUseCase.getPresignedUrl(account.getProfile().getImageKey());
                    return UserDto.from(account, account.getProfile(), url);
                });
    }
}

