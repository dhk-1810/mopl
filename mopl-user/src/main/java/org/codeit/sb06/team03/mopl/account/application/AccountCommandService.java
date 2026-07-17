package org.codeit.sb06.team03.mopl.account.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.application.in.*;
import org.codeit.sb06.team03.mopl.account.domain.Account;
import org.codeit.sb06.team03.mopl.account.domain.AccountService;
import org.codeit.sb06.team03.mopl.account.domain.exception.*;
import org.codeit.sb06.team03.mopl.common.error.InvalidIdentifierException;
import org.codeit.sb06.team03.mopl.account.domain.vo.EmailAddress;
import org.codeit.sb06.team03.mopl.account.domain.vo.Role;
import org.codeit.sb06.team03.mopl.follow.domain.Followee;
import org.codeit.sb06.team03.mopl.profile.domain.entity.Profile;
import org.codeit.sb06.team03.mopl.account.infra.out.AccountRepository;
import org.codeit.sb06.team03.mopl.account.infra.out.PasswordResetRepository;
import org.codeit.sb06.team03.mopl.profile.application.ProfileCommandService;
import org.codeit.sb06.team03.mopl.profile.application.in.CreateProfileCommand;
import org.codeit.sb06.team03.mopl.follow.application.FollowCommandService;
import org.codeit.sb06.team03.mopl.follow.application.in.CreateFollowCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AccountCommandService {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final ProfileCommandService profileCommandService;
    private final PasswordResetRepository passwordResetRepository;
    private final FollowCommandService followCommandService;

    @Transactional
    public Account register(RegisterAccountCommand command) {
        final String name = command.name();
        final EmailAddress emailAddress = command.emailAddress();
        final String rawPassword = command.rawPassword();

        if (accountRepository.existsByEmailAddress(emailAddress)) {
            throw new EmailAddressAlreadyExistsException(emailAddress.value());
        }
        Account newAccount = accountService.create(emailAddress, rawPassword);
        CompletableFuture<Profile> profile = CompletableFuture.supplyAsync(() -> {
            var profileCommand = new CreateProfileCommand(newAccount.getId(), name);
            return profileCommandService.create(profileCommand);
        }).exceptionally(throwable -> {
            throw new AccountRegistrationFailedException(throwable);
        });
        CompletableFuture<Followee> follow = CompletableFuture.supplyAsync(() -> {
            var followCommand = new CreateFollowCommand(newAccount.getId());
            return followCommandService.create(followCommand);
        });
        CompletableFuture.allOf(profile, follow).join();

        newAccount.setProfile(profile.join());

        accountRepository.save(newAccount);
        return newAccount;
    }

    @Transactional
    public Account resetPassword(ResetPasswordCommand command) {
        final EmailAddress emailAddress = command.emailAddress();

        Account existAccount = accountRepository.findByEmailAddress(emailAddress)
                .orElseThrow(() -> new EmailAddressNotFoundException(emailAddress));

        Account resetPasswordAccount = accountService.resetPassword(existAccount);

        accountRepository.save(resetPasswordAccount);
        return resetPasswordAccount;
    }

    @Transactional
    public void updatePassword(UUID accountId, UpdatePasswordCommand command) {

        // 불러오기
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        // 새 비밀번호로 변경
        accountService.updatePassword(account, command.newPassword());

        // 저장, 임시 비밀번호 삭제
        accountRepository.save(account);
        passwordResetRepository.deleteById(accountId);
    }

    @Transactional
    public void assignRole(UUID userId, AssignRoleCommand command) {
        // 제공받은 프로토 타입은 user-account를 분리하지 않았지만
        // 이벤트 스토밍 과정에서 user와 account가 분리되었고
        // 프론트엔드는 고정되어 있기에 현재 저희 프로젝트에서 userId는 AccountId의 의미로 사용되고 있습니다.
        final UUID accountUuid = userId;

        if (!Role.contains(command.role())) {
            throw new InvalidRoleException(command.role());
        }

        final Role role = Role.valueOf(command.role());

        Account foundAccount = accountRepository.findById(accountUuid)
                .orElseThrow(() -> new AccountNotFoundException(accountUuid));

        Account updatedAccount = accountService.updateRole(foundAccount, role);

        accountRepository.save(updatedAccount);
    }

    @Transactional
    public void updateLocked(UUID userId, UpdateLockStatusCommand command) {
        final UUID accountUuid = userId;
        final boolean locked = command.locked();

        Account foundAccount = accountRepository.findById(accountUuid)
                .orElseThrow(() -> new AccountNotFoundException(accountUuid));

        Account updatedAccount = accountService.updateLocked(foundAccount, locked);

        accountRepository.save(updatedAccount);
    }

    private UUID parseUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidIdentifierException(id);
        }
    }
}
