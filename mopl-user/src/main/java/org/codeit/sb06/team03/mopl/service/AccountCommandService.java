package org.codeit.sb06.team03.mopl.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.entity.Account;
import org.codeit.sb06.team03.mopl.entity.policy.PasswordEncryptionPolicy;
import org.codeit.sb06.team03.mopl.entity.policy.TempPasswordGenerationPolicy;
import org.codeit.sb06.team03.mopl.entity.policy.TempPasswordResetTimeoutPolicy;
import org.codeit.sb06.team03.mopl.entity.vo.EmailAddress;
import org.codeit.sb06.team03.mopl.entity.vo.Password;
import org.codeit.sb06.team03.mopl.entity.vo.Role;
import org.codeit.sb06.team03.mopl.exception.account.*;
import org.codeit.sb06.team03.mopl.entity.Followee;
import org.codeit.sb06.team03.mopl.entity.Profile;
import org.codeit.sb06.team03.mopl.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AccountCommandService {

    private final AccountRepository accountRepository;
    private final ProfileCommandService profileCommandService;
    private final FollowCommandService followCommandService;
    private final PasswordEncryptionPolicy passwordEncryptionPolicy;
    private final TempPasswordGenerationPolicy tempPasswordGenerationPolicy;
    private final TempPasswordResetTimeoutPolicy tempPasswordResetTimeoutPolicy;

    @Transactional
    public Account register(String name, EmailAddress emailAddress, String rawPassword) {
        if (accountRepository.existsByEmailAddress(emailAddress)) {
            throw new EmailAddressAlreadyExistsException(emailAddress.value());
        }
        Password password = passwordEncryptionPolicy.apply(rawPassword);
        Account newAccount = Account.create(emailAddress, password);
        CompletableFuture<Profile> profile = CompletableFuture.supplyAsync(() -> {
            return profileCommandService.create(newAccount.getId(), name);
        }).exceptionally(throwable -> {
            throw new AccountRegistrationFailedException(throwable);
        });
        CompletableFuture<Followee> follow = CompletableFuture.supplyAsync(() -> {
            return followCommandService.create(newAccount.getId());
        });
        CompletableFuture.allOf(profile, follow).join();

        newAccount.setProfile(profile.join());

        accountRepository.save(newAccount);
        return newAccount;
    }

    @Transactional
    public Account resetPassword(EmailAddress emailAddress) {
        Account existAccount = accountRepository.findByEmailAddress(emailAddress)
                .orElseThrow(() -> new EmailAddressNotFoundException(emailAddress));

        Account resetPasswordAccount = existAccount.passwordReset(
                tempPasswordGenerationPolicy,
                tempPasswordResetTimeoutPolicy,
                passwordEncryptionPolicy
        );

        accountRepository.save(resetPasswordAccount);
        return resetPasswordAccount;
    }

    @Transactional
    public void updatePassword(UUID accountId, String newPassword) {

        // 불러오기
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        // 새 비밀번호로 변경
        Password password = passwordEncryptionPolicy.apply(newPassword);
        account.updatePassword(password);

        // 저장
        accountRepository.save(account);
    }

    @Transactional
    public void assignRole(UUID userId, String roleName) {
        final UUID accountUuid = userId;

        if (!Role.contains(roleName)) {
            throw new InvalidRoleException(roleName);
        }

        final Role role = Role.valueOf(roleName);

        Account foundAccount = accountRepository.findById(accountUuid)
                .orElseThrow(() -> new AccountNotFoundException(accountUuid));

        Account updatedAccount = foundAccount.updateRole(role);

        accountRepository.save(updatedAccount);
    }

    @Transactional
    public void updateLocked(UUID userId, boolean locked) {
        final UUID accountUuid = userId;

        Account foundAccount = accountRepository.findById(accountUuid)
                .orElseThrow(() -> new AccountNotFoundException(accountUuid));

        Account updatedAccount = foundAccount.updateLocked(locked);

        accountRepository.save(updatedAccount);
    }

}
