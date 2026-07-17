package org.codeit.sb06.team03.mopl.security;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.domain.Account;
import org.codeit.sb06.team03.mopl.account.exception.AccountNotFoundException;
import org.codeit.sb06.team03.mopl.account.domain.vo.EmailAddress;
import org.codeit.sb06.team03.mopl.account.repository.AccountRepository;
import org.codeit.sb06.team03.mopl.image.service.ImageQueryService;
import org.codeit.sb06.team03.mopl.profile.domain.entity.Profile;
import org.codeit.sb06.team03.mopl.profile.controller.UserDto;
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
    private final ImageQueryService imageQueryService;

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

