package org.codeit.sb06.team03.mopl.security;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.entity.Account;
import org.codeit.sb06.team03.mopl.exception.account.AccountNotFoundException;
import org.codeit.sb06.team03.mopl.entity.vo.EmailAddress;
import org.codeit.sb06.team03.mopl.repository.AccountRepository;
import org.codeit.sb06.team03.mopl.service.cqrs.ExternalImageQueryService;
import org.codeit.sb06.team03.mopl.entity.Profile;
import org.codeit.sb06.team03.mopl.dto.response.UserDto;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MoplUserDetailsService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final ExternalImageQueryService imageQueryService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        EmailAddress emailAddress = new EmailAddress(email);
        Account account = accountRepository.findByEmailAddress(emailAddress)
                .orElseThrow(() -> new AccountNotFoundException(emailAddress));

        UserDto userDto = getUserDto(account);

        return new MoplUserDetails(userDto, account.getPassword().value());
    }

    private UserDto getUserDto(Account account) {
        UUID accountId = account.getId();

        Profile profile = account.getProfile();
        String profileImageUrl = imageQueryService.getPresignedUrl(profile.getImageKey());

        UserDto userDto = new UserDto(
                accountId,
                account.getCreatedAt(),
                account.getEmailAddress().value(),
                profile.getName(),
                profileImageUrl,
                account.getRole().name(),
                account.isLocked()
        );
        return userDto;
    }
}

