package org.codeit.sb06.team03.mopl.composite;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.application.in.*;
import org.codeit.sb06.team03.mopl.account.domain.Account;
import org.codeit.sb06.team03.mopl.content.application.in.GetContentUseCase;
import org.codeit.sb06.team03.mopl.profile.application.in.UpdateProfileCommand;
import org.codeit.sb06.team03.mopl.profile.application.in.UpdateProfileUseCase;
import org.codeit.sb06.team03.mopl.profile.domain.entity.Profile;
import org.codeit.sb06.team03.mopl.profile.infra.ProfileMapper;
import org.codeit.sb06.team03.mopl.profile.infra.in.*;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.GetWatchingSessionUseCase;
import org.codeit.sb06.team03.mopl.common.cache.ProfileImageCache;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserCompositeService {

    private final AccountMapper accountMapper;
    private final ProfileMapper profileMapper;
    private final RegisterAccountUseCase registerAccountUseCase;
    private final AssignRoleUseCase assignRoleUseCase;
    private final UpdateLockStatusUseCase updateLockStatusUseCase;
    private final UpdatePasswordUseCase updatePasswordUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;

    private final GetWatchingSessionUseCase getWatchingSessionUseCase;
    private final ProfileImageCache profileImageCache;

    public UserDto registerAccount(UserCreateRequest request) {
        RegisterAccountCommand command = accountMapper.toCommand(request);
        Account newAccount = registerAccountUseCase.register(command);

        return getAccountUseCase.get(newAccount.getId());
    }

    public void updatePassword(UUID userId, PasswordUpdateRequest request) {
        UpdatePasswordCommand command = accountMapper.toCommand(request);
        updatePasswordUseCase.updatePassword(userId, command);
    }

    public void assignUserRole(UUID userId, UserRoleUpdateRequest request) {
        AssignRoleCommand command = accountMapper.toCommand(request);
        assignRoleUseCase.assignRole(userId, command);
    }

    public void updateUserLockStatus(UUID userId, UserLockUpdateRequest request) {
        UpdateLockStatusCommand command = accountMapper.toCommand(request);
        updateLockStatusUseCase.updateLocked(userId, command);
    }

    public CursorResponseUserDto getUsers(CursorRequestUserDto request) {
        return getAccountUseCase.get(request);
    }

    public UserDto getUser(UUID userId) {
        return getAccountUseCase.get(userId);
    }

    public UserDto updateProfile(UUID userId, UserUpdateRequest request, @Nullable MultipartFile image) {
        UpdateProfileCommand command = profileMapper.toCommand(userId, request, image);
        Profile updated = updateProfileUseCase.update(command);
        profileImageCache.evictProfileImageUrl(userId);
        return getAccountUseCase.get(updated.getAccountId());
    }
}
